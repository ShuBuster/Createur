package com.example.createurdemploidutemps;

import java.io.Serializable;
import java.util.ArrayList;

import modele.EmploiDuTemps;
import services.Bluetooth;
import services.Bluetooth_Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import boutons.Bouton;

import com.example.basededonnees.EmploiDAO;
import com.example.controler.Createur_Presenter;
import com.example.createurdemploidutemps.EmploiActivity;

import composants.AnimatedText;
import composants.Animer;
import composants.Utile;

public class MainActivity extends Activity {

	private static final String TAG = "MAIN_APP";

	private ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();

	private LinearLayout liste;

	private int ID = 1;

	// acces a la base de donnes

	private final EmploiDAO emploidao = new EmploiDAO(this);

	// attributs de taille d'ecran, de texte et de boutons
	private int H;

	private float textSize = 50;

	private final int unit = TypedValue.COMPLEX_UNIT_FRACTION;

	private final float taille_boutons = 0.5f;

	private final Activity a = this;

	private EditText monTexte;

	private Button validerEDT;

	private final Createur_Presenter _presenter = new Createur_Presenter(this);

	/** whether the bluetooth is enabled */
	private boolean enable;

	/**
	 * {@inheritDoc}.
	 */
	
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ask for bluetooth permission
		//final Intent enableIntent = new Intent(
		//		BluetoothAdapter.ACTION_REQUEST_ENABLE);
		//startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

		// full screen
		// Utile.fullScreen(this);
		setContentView(R.layout.activity_main);

		// taille ecran
		H = Utile.getScreenSize(a)[1];
		final int wDpi = Utile.getScreenSizePI(a)[0];
		Log.d(TAG, "height : " + H + " , wDpi : " + wDpi);
		if (wDpi > 300) {
			textSize = 80;
		}
		if (H < 1000) {
			textSize = 30;
		}

		// ///////////////////////////////////////////////////////////////////////
		// / IHM
		// ///////////////////////////////////////////////////////////////////////

		// titres
		final LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre_edt);
		final int[] colors = { R.color.light_green3, R.color.light_green4,
				R.color.light_green5, R.color.green4, R.color.green5,
				R.color.blue3, R.color.blue5, R.color.red2, R.color.pink2,
				R.color.pink3, R.color.red3, R.color.pink4, R.color.red4,
				R.color.red5, R.color.red6 };
		AnimatedText.add(this, layout_titre, "Creation de frise", colors,
				textSize + 20, unit);

		// Reglage de la marge du deuxieme titre
		final TextView texte_edt = (TextView) findViewById(R.id.edt_titre);
		texte_edt.setTextSize(unit, textSize);
		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.setMargins(0, H / 5, 0, 0);
		texte_edt.setLayoutParams(params);

		/* Apparition du logo bouton */
		final Button logo_bouton = (Button) findViewById(R.id.logo_bouton);
		final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
		final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
		final ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
		Animer.activityApparitionAnimation(logo_bouton, slide_bottom,
				slide_top, shadow, H);

		// IHM bouton de validation
		validerEDT = (Button) findViewById(R.id.valider_edt);
		validerEDT.setId(89);
		final Drawable creerEDT_d = Bouton.roundedDrawable(this,
				R.color.light_blue3, taille_boutons);
		validerEDT.setBackgroundDrawable(creerEDT_d);

		monTexte = (EditText) findViewById(R.id.mon_texte);

		// ////////////////////////////////////////////////////////////////////////////////////
		// / Listeners sur les bouton et Edittexte du bas
		// ///////////////////////////////////////////////////////////////////////////////////

		monTexte.addTextChangedListener(new TextWatcher() {

			
			public void afterTextChanged(final Editable s) {
				validerEDT.setAlpha(1f);
				validerEDT.setEnabled(true);
				validerEDT.setText(getResources().getString(
						R.string.valider_emploi));
			}

			
			public void beforeTextChanged(final CharSequence s,
					final int start, final int count, final int after) {
			}

			
			public void onTextChanged(final CharSequence s, final int start,
					final int before, final int count) {

			}
		});

		// listener pour la creation d'edt
		validerEDT.setOnClickListener(new OnClickListener() {

			
			public void onClick(final View v) {
				// valide le nouvel edt
				if (!monTexte.getText().toString().equals("")) {
					if (NomUnique()) {
						final EmploiDuTemps edt = new EmploiDuTemps(ID, null,
								monTexte.getText().toString(), null);
						edt.setValid(false);
						ID++;
						emplois.add(edt);
						addLayoutEDT(edt);
						monTexte.setText("");
						setValider();
						// ajout a la base de donnees
						emploidao.ajouter(edt);
					}
				}
				// envoi tous les edt par bluetooth
				else {
					// envoi par bluetooth si le bluetooth est actif
					enable = _presenter.createConnection(mHandler);
					if (enable) {
						// envoi des edt valides
						ArrayList<EmploiDuTemps> edtValides = new ArrayList<EmploiDuTemps>();
						for(EmploiDuTemps emploi : emplois){
							if(emploi.isValid()){
								edtValides.add(emploi);
							}
						}
						_presenter.syncBluetooth(edtValides);
					}
				}

			}
		});

		// //////////////////////////////////////////////////////////////////////////////////////
		// bouton d'aide et alert dialog
		// /////////////////////////////////////////////////////////////////////////////////////
		Button bouton_aide = (Button) findViewById(R.id.aide);
		final String aide_texte = getResources().getString(R.string.texte_aide);
		bouton_aide.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				AlertDialog aideDialog = new AlertDialog.Builder(a)
						.setTitle(getResources().getString(R.string.aide_title))
						.setMessage(aide_texte)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								})
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
				
				aideDialog.setIcon(getResources().getDrawable(R.drawable.help));
				
			}
		});
	}

	/**
	 * {@inheritDoc}.
	 */
	
	public void onResume() {
		super.onResume();

		// remise a jour de l'ihm
		liste = (LinearLayout) findViewById(R.id.liste_emplois);
		liste.removeAllViews();
		setValider();

		_presenter.createConnection(mHandler);

		// ajouts des emplois du temps depuis la base de donnees
		emplois = _presenter.chargeEmplois();

		for (final EmploiDuTemps emploi : emplois) {
			addLayoutEDT(emploi);
		}

	}

	/**
	 * {@inheritDoc}.
	 */
	
	protected void onDestroy() {
		super.onDestroy();
		_presenter.destroy(emplois);
	}

	/**
	 * met a jour l'IHM du bouton valider
	 */
	private void setValider() {
		// s'il y a du bluetooth :
		if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			validerEDT.setText(getResources().getString(
					R.string.envoi_bluetooth));
			validerEDT.setAlpha(1f);
			validerEDT.setEnabled(true);
		} else {
			validerEDT.setText(getResources()
					.getString(R.string.valider_emploi));
			validerEDT.setAlpha(0.3f);
			validerEDT.setEnabled(false);
		}
	}

	/**
	 * ajoute graphiquement l'emploi du temps, avec les boutons renommer et
	 * supprimer
	 * 
	 * @param emploi
	 * @param liste2
	 */
	private void addLayoutEDT(final EmploiDuTemps emploi) {
		if (emploi.getNomEnfant() != null || !emploi.getNomEnfant().equals("")) {
			final RelativeLayout layout = new RelativeLayout(
					getApplicationContext());
			liste.addView(layout);
			final LayoutParams params = new LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 0, H / 20);
			layout.setLayoutParams(params);
			// ///////////////////////////////////////////////////////////////////////////////

			// bouton de suppression de l'edt

			final Button suppr = new Button(getApplicationContext());
			layout.addView(suppr);
			suppr.setId(123);
			suppr.setText(R.string.suppr);
			final RelativeLayout.LayoutParams suppr_params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			suppr_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			suppr_params.setMargins(0, 0, 10, 0);
			suppr.setLayoutParams(suppr_params);
			final Drawable suppr_d = Bouton.roundedDrawable(this, R.color.red7,
					taille_boutons);
			suppr.setBackgroundDrawable(suppr_d);

			suppr.setOnClickListener(new OnClickListener() {

				
				public void onClick(final View v) {

					// //////////////////////////////////////////////////////////////////////////
					// / ALertdialog de confirmation de suppression
					// //////////////////////////////////////////////////////////////////////////

					new AlertDialog.Builder(a)
							.setTitle(
									getResources().getString(
											R.string.suppr_conf_title))
							.setMessage(
									getResources().getString(
											R.string.suppr_conf_message))
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									})
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
											liste.removeView(layout);
											emplois.remove(emploi);
											emploidao.supprimer(emploi);
											ID--;
											dialog.cancel();
										}
									})
							.setIcon(android.R.drawable.ic_dialog_alert).show();

				}
			});
			// //////////////////////////////////////////////////////////////////////////////

			// Nom de l'emploi du temps, suivi par les bouton renommer et
			// supprimer
			final TextView nom = new TextView(getApplicationContext());
			layout.addView(nom);
			final RelativeLayout.LayoutParams nom_params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			nom_params.setMargins(10, 0, 0, 0);
			nom.setLayoutParams(nom_params);
			nom.setId(1560);
			nom.setText(emploi.getNomEnfant());

			// la couleur du texte change selon que l'emploi est valide ou non
			if (emploi.isValid()) {
				nom.setTextColor(getResources().getColor(R.color.indigo5));
			} else {
				nom.setTextColor(getResources().getColor(R.color.red7));
			}
			nom.setTextSize(unit, textSize);

			nom.setOnClickListener(new OnClickListener() {

				
				public void onClick(final View v) {
					final Intent secondActivity = new Intent(a,
							EmploiActivity.class);
					final Serializable extra = emploi;
					if (extra != null) {
						secondActivity.putExtra("extra", emploi);
					}
					a.startActivity(secondActivity);
				}
			});

			// /////////////////////////////////////////////////////////////////////////////

			// bouton pour renommer l'edt
			final Button renommer = new Button(getApplicationContext());
			layout.addView(renommer);
			renommer.setId(1541);
			renommer.setText(R.string.renom);
			final RelativeLayout.LayoutParams renommer_params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			renommer_params.addRule(RelativeLayout.ALIGN_LEFT,
					validerEDT.getId());
			renommer_params.addRule(RelativeLayout.LEFT_OF, suppr.getId());
			renommer_params.setMargins(0, 0, 10, 0);
			renommer.setLayoutParams(renommer_params);
			final Drawable renommer_d = Bouton.roundedDrawable(this,
					R.color.vert3, taille_boutons);
			renommer.setBackgroundDrawable(renommer_d);

			// listener pour la creation d'edt
			renommer.setOnClickListener(new OnClickListener() {

				
				public void onClick(final View v) {
					if (!monTexte.getText().toString().equals("")) {
						if (NomUnique()) {
							String ancientNom = emploi.getNomEnfant();
							emploi.setNomEnfant(monTexte.getText().toString());
							monTexte.setText("");
							nom.setText(emploi.getNomEnfant());
							setValider();

							// modifie le nom dans la base de donnees
							emploidao.modifier(ancientNom, emploi);

						}
					} else {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.nom_vide),
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}

	/**
	 * verifie l'unicite du nom de l'edt, sinon bugs de base de donnees ...
	 * 
	 * @return
	 */
	private boolean NomUnique() {
		for (final EmploiDuTemps edt : emplois) {
			if (monTexte.getText().toString().equals(edt.getNomEnfant())) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.nom_unique),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	/**
	 * The Handler that gets information back from the BluetoothService Then
	 * display informations or redirect it to the service class to perform an
	 * action.
	 */
	private final Handler mHandler = new Handler() {

		
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case Bluetooth_Constants.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case Bluetooth.STATE_CONNECTED:
					Toast.makeText(getApplicationContext(), "STATE_CONNECTED",
							Toast.LENGTH_SHORT).show();
					break;
				case Bluetooth.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(), "STATE_CONNECTING",
							Toast.LENGTH_SHORT).show();
					break;
				case Bluetooth.STATE_LISTEN:
					Toast.makeText(getApplicationContext(), "STATE_LISTEN",
							Toast.LENGTH_SHORT).show();
					break;
				case Bluetooth.STATE_NONE:
					Toast.makeText(getApplicationContext(), "STATE_NONE",
							Toast.LENGTH_SHORT).show();
					break;
				}
				break;
			case Bluetooth_Constants.MESSAGE_WRITE:
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.send),
						Toast.LENGTH_SHORT).show();
				break;
			case Bluetooth_Constants.MESSAGE_READ:
				// MAJ de la base de donnees envoyee par Bluetooth
				emplois = _presenter.getEdtBluetooth(msg.obj);
				for (final EmploiDuTemps emploi : emplois) {
					addLayoutEDT(emploi);
				}
				
				break;
			case Bluetooth_Constants.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				final String deviceName = msg.getData().getString(
						Bluetooth_Constants.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connection a " + deviceName + " ...",
						Toast.LENGTH_SHORT).show();
				break;
			case Bluetooth_Constants.MESSAGE_TOAST:

				Toast.makeText(getApplicationContext(),
						msg.getData().getString(Bluetooth_Constants.TOAST),
						Toast.LENGTH_SHORT).show();
				break;

			}
		}

	};

}
