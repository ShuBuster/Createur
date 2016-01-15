package com.example.controler;

/*
 * Projet SIT, @copyright 2015 SAGEM DS
 * Les informations contenues dans ce fichier sont la propriété de
 * SAGEM DS et diffusées à titre confidentiel dans un but spécifique.
 * Le destinataire assure la garde et la surveillance de ce fichier et
 * convient qu'il ne sera ni copié ni reproduit en tout ou partie et
 * que son contenu ne sera révélé en aucune manière à aucune personne,
 * excepté pour répondre au but pour le quel il a été transmis.
 * Cette recommandation est applicable à tous les documents générés à
 * partir de ce fichier.
 */

import java.util.ArrayList;
import java.util.Set;

import modele.EmploiDuTemps;
import modele.HeuresMarquees;
import modele.Task;
import services.Bluetooth;
import services.Storage;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.basededonnees.EmploiDAO;
import com.example.basededonnees.HeuresDAO;
import com.example.basededonnees.TaskDAO;
import com.example.createurdemploidutemps.R;

/**
 * @author local
 */
public class Createur_Presenter {

	private static String TAG = "CREATEUR_APP";

	private final Context _context;

	// acces a la base de donnes

	private final EmploiDAO emploidao;

	private final TaskDAO taskdao;

	private final HeuresDAO heuredao;

	// connection attributes

	/** The bluetooth device paired with the phone. */
	private BluetoothDevice _device;

	/** The bluetooth adapter is the basis of bluetooth connection. */
	private final BluetoothAdapter _blueAdapter;

	/** Service for a bluetooth/network connection. */
	private Bluetooth _service = null;

	/**
	 * Constructor.
	 * 
	 * @param _context
	 */
	public Createur_Presenter(final Context _context) {
		super();
		this._context = _context;
		_device = null;
		_blueAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (_blueAdapter == null) {
			Toast.makeText(_context, "Le Bluetooth n'est pas supporte",
					Toast.LENGTH_SHORT).show();
		}
		_service = null;
		emploidao = new EmploiDAO(_context);
		taskdao = new TaskDAO(_context);
		heuredao = new HeuresDAO(_context);

	}

	/**
	 * Stop the service's Threads, end the connection. Save all the datas into
	 * the backup file.
	 * 
	 * @param emplois
	 */
	public void destroy(ArrayList<EmploiDuTemps> emplois) {
		if (_service != null) {
			_service.stop();
		}
		// sauvegardeFichier(emplois);

	}

	/**
	 * Search a device to be paired with
	 */
	private void searchDevice() {
		_blueAdapter.startDiscovery();
		final Set<BluetoothDevice> pairedDevices = _blueAdapter
				.getBondedDevices();

		if (pairedDevices.size() <= 0) {
			return;
		}
		for (final BluetoothDevice device : pairedDevices) {
			Log.d(TAG, "Device found : " + device.getName());
			if (null == _device) {
				_device = device;
				Log.d(TAG, "Choosing device " + _device.getName());
			}
		}
		_blueAdapter.cancelDiscovery();
	}

	/**
	 * Si le bluetooth est actif, tente d'etablir une connexion avec l'appli
	 * FRIZZ. Si le bluetooth n'est pas actif, ne fait rien.
	 * 
	 * @param mHandler
	 *            a Handler to allow communication with the presenter and the
	 *            UI.
	 * @return wether the connection is initialized.
	 */
	public boolean createConnection(final Handler mHandler) {

		// check if the device support bluetooth
		if (_blueAdapter == null) {
			return false;
		}
		if (_blueAdapter.isEnabled()) {

			// do this once, if there is no connection service yet
			if (_service == null) {
				// Initialize the BluetoothService to perform bluetooth
				// connections
				_service = new Bluetooth(_context, mHandler);
				searchDevice();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// running connection
			if (_service.getState() == Bluetooth.STATE_CONNECTED) {
				return true;
			}
			// not connected = first time or no devices
			else {
				_service.connect(_device, true);
				return _service.getState() == Bluetooth.STATE_CONNECTED;
			}
		}
		return false;

	}

	/**
	 * Recupere tous les emplois du temps envoyes par Bluetooth et remet a jour
	 * la base de donnee.
	 * 
	 * @param obj
	 * @return la liste des emplois charges (vide au pire)
	 */
	public ArrayList<EmploiDuTemps> getEdtBluetooth(Object obj) {
		final byte[] bytes = (byte[]) (obj);
		final ArrayList<EmploiDuTemps> edt = EmploiDuTemps.deserialize(bytes);
		if (!edt.isEmpty()) {
			Toast.makeText(_context,
					_context.getResources().getString(R.string.emplois_recus),
					Toast.LENGTH_SHORT).show();

			// remise a jour de la base de donnees
			updateDataBase(edt);
		}
		return emploidao.getEmplois(taskdao, heuredao);

	}

	/**
	 * met a jour la base de donnees
	 * 
	 * @param emplois
	 *            Les emplois du temps a inserer/ modifier dans la base
	 */
	private void updateDataBase(ArrayList<EmploiDuTemps> emplois) {

		ArrayList<EmploiDuTemps> edtDB = emploidao
				.getEmplois(taskdao, heuredao);
		for (EmploiDuTemps emploi : emplois) {
			// checks if this edt already exists in the dataBase (i<size)
			int i = 0;
			while (i < edtDB.size()
					&& !edtDB.get(i).getNomEnfant()
							.equals(emploi.getNomEnfant())) {
				i++;
			}

			if (i < edtDB.size()) {
				emploidao.modifier(emploi.getNomEnfant(), emploi);
				for (Task t : emploi.getEmploi()) {
					taskdao.modifier(t);
				}
				for (HeuresMarquees h : emploi.getMarqueTemps()) {
					heuredao.modifier(h);
				}
			} else {
				emploidao.ajouter(emploi);
				for (Task t : emploi.getEmploi()) {
					taskdao.ajouter(t);
				}
				for (HeuresMarquees h : emploi.getMarqueTemps()) {
					heuredao.ajouter(h);
				}
			}
		}

	}

	/**
	 * Recupere tous les emplois du temps de la base de donnees ou du fichier de
	 * sauvegarde si cette derniere est vide, remet a jour la BD dans ce cas.
	 * 
	 * @return la liste des emplois charges (vide au pire)
	 */
	public ArrayList<EmploiDuTemps> chargeEmplois() {

		// lecture depuis la base de donnees
		ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();
		emplois = emploidao.getEmplois(taskdao, heuredao);
		/*
		 * if (emplois.isEmpty()) {
		 * 
		 * // lecture depuis le fichier si la base de donnees est vide
		 * 
		 * emplois = EmploiDuTemps.deserialize(null); Toast.makeText( _context,
		 * _context.getResources().getString( R.string.charge_emplois_fichier),
		 * Toast.LENGTH_SHORT).show();
		 * 
		 * // remise a jour de la base de donnees for (final EmploiDuTemps edt :
		 * emplois) { if (edt != null) { emploidao.ajouter(edt); for (Task t :
		 * edt.getEmploi()) { taskdao.ajouter(t); } for (HeuresMarquees h :
		 * edt.getMarqueTemps()) { heuredao.ajouter(h); } } }
		 * 
		 * }
		 * 
		 * // synchronize les bases de la tablette et du Smartphone.
		 * syncBluetooth(emplois);
		 */

		return emplois;
	}

	/**
	 * Envoi tous les edt contenus dans la base de donnees pour mettre a jour la
	 * base de donnees du Smartphone ou de la tablette par Bluetooth.
	 * 
	 * @param emplois
	 */
	public void syncBluetooth(ArrayList<EmploiDuTemps> emplois) {

		// check if the device support bluetooth
		if (_blueAdapter == null) {
			return;
		}
		// Check that we're actually connected before trying anything
		if (!_blueAdapter.isEnabled()
				|| _service.getState() != Bluetooth.STATE_CONNECTED) {
			Toast.makeText(_context, R.string.not_connected_bluetooth,
					Toast.LENGTH_SHORT).show();
			return;

		}

		// Check that there's actually something to send
		if (!emplois.isEmpty()) {
			// Get the message bytes and tell the BluetoothService to write
			final byte[] send = EmploiDuTemps.serialize(emplois);
			_service.write(send);
		}

	}

	/**
	 * Sauvegarde toutes la base de donnees dans un fichier de sauvegarde.
	 * 
	 * @param emplois
	 */
	private void sauvegardeFichier(ArrayList<EmploiDuTemps> emplois) {
		if (!emplois.isEmpty()) {
			final byte[] bytes = EmploiDuTemps.serialize(emplois);
			Storage.writeFriseFile(bytes);
			Toast.makeText(_context,
					_context.getResources().getString(R.string.sauvegarde),
					Toast.LENGTH_SHORT).show();
		}

	}

}
