package com.latarce.location.dondeestamicoche;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Geocoder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.latarce.location.dondeestamicoche.R;


public class DondeActivity extends MapActivity implements OnClickListener{

	private static final int DEFAULT_ZOOM_LEVEL = 18;
	private static final int DEFAULT_ZOOM_LEVEL_SPDL = 18;
	private static final String POSITION_SAVED = "positionSaved";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	
	private SharedPreferences positionSaved;
	private SharedPreferences.Editor editor;
	
	private Button btCenter;
	private ToggleButton btSatelite;
	private Button btSPDLCenter;
	private Button btSavePosition;
	private Button btInfo;
	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;
	private MapView mapView;
	private Location carLocation;
	private Location myLoc;
	private MapController mapControl;
	private MyLocationOverlay me = null;
	private Geocoder geocoder;
	private SitesOverlay sitesOverlay = null;
	private List<Address> car_address = new ArrayList<Address>();
	
	private String myPositionTextInfo;
	private String myShortPositionTextInfo;
	private String myShortCarTextInfo;
	private String myShortDistanceTextInfo;
	
	private Drawable marker;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_donde_esta_mi_coche);
        
        if (!checkConnectionStatus())
        	finish();
		
		//PREFERENCIAS -> Posicion guardada
		positionSaved = getSharedPreferences(POSITION_SAVED, MODE_PRIVATE);
		
		//BOTONES
		btCenter = (Button)this.findViewById(R.id.btCenter);
		btCenter.setOnClickListener(this);
		btSPDLCenter = (Button)this.findViewById(R.id.btSPDLCenter);
		btSPDLCenter.setOnClickListener(this);
		btSatelite = (ToggleButton)this.findViewById(R.id.btSatelite);
		btSatelite.setOnClickListener(this);
		btSavePosition = (Button)this.findViewById(R.id.btSavePosition);
		btSavePosition.setOnClickListener(this);
		btInfo = (Button)this.findViewById(R.id.btInfo);
		btInfo.setOnClickListener(this);
		
		//COORDENADAS COCHE
		carLocation = new Location("GPS_PROVIDER");
		carLocation.setLatitude(positionSaved.getFloat(LATITUDE, 0));
		carLocation.setLongitude(positionSaved.getFloat(LONGITUDE, 0));
		
		
		//GEOCODER
		geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

		
		//MAPA
		mapView = (MapView) findViewById(R.id.myMapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);
		btSatelite.setChecked(false);
		
		//Control del mapa
		mapControl = mapView.getController();
		
		//Seleccion del icono del coche que se dibujará sobre el mapa
		marker = getResources().getDrawable(R.drawable.coche);
		//Se centra el icono del coche sobre el punto
	    marker.setBounds(marker.getIntrinsicWidth()/-2, marker.getIntrinsicHeight()/-2, marker.getIntrinsicWidth()/2, marker.getIntrinsicHeight()/2);
		//Seleccion del icono de puntero de brújula que se dibujará sobre el mapa
		Bitmap pointer = BitmapFactory.decodeResource(getResources(), R.drawable.flecha);
	    
	    //Añadir posiciones
	    sitesOverlay = new SitesOverlay(marker);
	    mapView.getOverlays().add(sitesOverlay);
	    me=new CurrentLocationOverlay(this, mapView, pointer);
	    mapView.getOverlays().add(me);
	    
	    
		//LOCALIZADOR		
    	//Obtenemos una referencia al LocationManager
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		//Obtenemos la última posición conocida (Si el proveedor está actualmente deshabilitado obtenemos un null pointer)
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			myLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			myLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
    	//Mostramos la última posición conocida
		//MostrarPosicionThread mostrarPosicionThread = new MostrarPosicionThread();
		//mostrarPosicionThread.start();
		if (myLoc != null){
			mostrarPosicion();
		}

    	
    	//Nos registramos para recibir actualizaciones de la posición
		mLocationListener = new MyLocationListener();
		
		//Actualizar la posición cada 2 ó 30 segundos, con una distancia minima de 0 metros
		requestLocation();
    }

    /*
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		// Checks the orientation of the screen
	    //if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    //    Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    //} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    //    Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    //}
		 
		if (mapView.isSatellite()){
			btSatelite.setChecked(true);
		}else{
			btSatelite.setChecked(false);
		}
	}
    
    class MostrarPosicionThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			mostrarPosicion();
		}
    	
    }
    */
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_donde_esta_mi_coche, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_position:
				Intent j = new Intent(this, Position.class);
				j.putExtra("position", myShortPositionTextInfo);
				j.putExtra("car_position", myShortCarTextInfo);
				j.putExtra("distance", myShortDistanceTextInfo);
				startActivity(j);
				break;
				
			case R.id.menu_share:
				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
				//set the type  
				shareIntent.setType("text/plain");  
				//add a subject  
				shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
				//build the body of the message to be shared  
				String shareMessage = getResources().getString(R.string.share_message);  
				//add the message  
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);  
				//start the chooser for sharing  
				startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_chooser)));  
				break;
				
			case R.id.menu_info:
				Intent l = new Intent(this, About.class);
				startActivity(l);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume(){
        if (!checkConnectionStatus())
        	finish();
		me.enableMyLocation();
		me.enableCompass();
		requestLocation();
		super.onResume();
	}
	
	@Override
	protected void onPause(){
		me.disableMyLocation();
		me.disableCompass();
		mLocationManager.removeUpdates(mLocationListener);
		super.onPause();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btCenter:
				animarPosicionActual();
				break;
			case R.id.btSPDLCenter:
				animarPosicionSPDL();
				break;
			case R.id.btSatelite:
		        if(mapView.isSatellite())
		        	mapView.setSatellite(false);
		        else
		        	mapView.setSatellite(true);
				break;
			case R.id.btSavePosition:
				guardarPosicion();
				break;
			case R.id.btInfo:
				openOptionsMenu();
				break;
		}
	}
	
	private class MyLocationListener implements LocationListener{
		@Override
		public void onLocationChanged(Location loc) {
			myLoc = new Location(loc);
			mostrarPosicion();
		}
		@Override
		public void onProviderDisabled(String provider) {
			// TODO
		}
		@Override
		public void onProviderEnabled(String provider) {
			// TODO
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO
		}
	}
	
	private void guardarPosicion() {
		if(myLoc != null){
			editor = positionSaved.edit();
			editor.putFloat(LATITUDE, (float)myLoc.getLatitude());
			editor.putFloat(LONGITUDE, (float)myLoc.getLongitude());
			editor.commit();
			//Mostrar posición
		    carLocation.setLatitude(positionSaved.getFloat(LATITUDE, 0));
			carLocation.setLongitude(positionSaved.getFloat(LONGITUDE, 0));
			mostrarPosicion();
			mapView.invalidate();
			animarPosicionSPDL();
	    }
		else{
			Toast.makeText(getBaseContext(), getResources().getString(R.string.error_saving), Toast.LENGTH_LONG).show();
		}
	}
	
	private void animarPosicionActual() {
	    if(myLoc != null){
			GeoPoint geoPoint = new GeoPoint(
					(int) (myLoc.getLatitude()* 1000000),
					(int) (myLoc.getLongitude()* 1000000));
			mapControl.setZoom(DEFAULT_ZOOM_LEVEL);
			mapControl.animateTo(geoPoint);
	    }
		else{
			Toast.makeText(getBaseContext(), getResources().getString(R.string.error_position), Toast.LENGTH_LONG).show();
		}
	}
	
	private void animarPosicionSPDL() {
		mapControl.setZoom(DEFAULT_ZOOM_LEVEL_SPDL);
		mapControl.animateTo(new GeoPoint((int)(carLocation.getLatitude()*1000000), (int)(carLocation.getLongitude()*1000000)));
	}
	
	private void mostrarPosicion() {
	    if(myLoc != null){
	    	List<Address> addresses = new ArrayList<Address>();
	    	int altitud = (int)myLoc.getAltitude();
	    	int precision = (int)myLoc.getAccuracy();
	    	String str_altitud = "";
	    	    	
		    //DATOS
	    	if (altitud != 0){
	    		str_altitud = getResources().getString(R.string.data_altitude_yes, altitud);
	    	}else{
	    		str_altitud = getResources().getString(R.string.data_altitude_no);
	    	}
	    	myPositionTextInfo = getResources().getString(R.string.data_my_position, 
	    			Math.rint(myLoc.getLatitude()*1000000)/1000000,
	    			Math.rint(myLoc.getLongitude()*1000000)/1000000,
	    			str_altitud,
	    			myLoc.getProvider());
	    	
		    //GEOCODER
		    try{
		    	// Obtener la dirección del usuario
		    	addresses = geocoder.getFromLocation(myLoc.getLatitude(), myLoc.getLongitude(), 1);
		    	myShortPositionTextInfo = addresses.get(0).getAddressLine(0)
		    						 + "\n" + addresses.get(0).getAddressLine(1)
		    						 + "\n" + addresses.get(0).getAddressLine(2) + "\n";
		    	
		    	// Obtener la dirección del coche
		    	car_address = geocoder.getFromLocation(carLocation.getLatitude(), carLocation.getLongitude(), 1);
		    	myShortCarTextInfo = car_address.get(0).getAddressLine(0)
		    						 + "\n" + car_address.get(0).getAddressLine(1)
		    						 + "\n" + car_address.get(0).getAddressLine(2) + "\n";
		    	
		    	// Obtener la distancia entre ambos
		    	myShortDistanceTextInfo = getResources().getString(R.string.data_distance, 
		    			(int)Math.rint(myLoc.distanceTo(carLocation)), 
		    			precision);
		    		
		    }catch(Exception e) {
		    	e.printStackTrace();
		    }
		    
		    myPositionTextInfo = myShortPositionTextInfo + "\n" + myShortDistanceTextInfo + myPositionTextInfo;
	    }
	    else{
	    	myPositionTextInfo = getResources().getString(R.string.error_position);   	
	    }
		// Actualizar el texto de la posición del coche
		sitesOverlay.updateCarPosition(false);
	}

	private void requestLocation(){
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);
		}
		else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, mLocationListener);
		}
		else{
			Toast.makeText(getBaseContext(), getResources().getString(R.string.error_gps), Toast.LENGTH_LONG).show();
			finish();			
		}
	}
	
    // Check connection Status
    private boolean checkConnectionStatus(){
    	/*
    	ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	//mobile
    	State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
    	//wifi
    	State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
    	
    	if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
    	    //mobile
    		return true;
    	} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
    		return true;
    	} else{
    		Toast.makeText(this.getBaseContext(), "Conexión a la red no disponible\nNecesita activar el GPS o la localización por red", Toast.LENGTH_LONG).show();
    		return false;
    	}
    	*/

    	ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = conMan.getActiveNetworkInfo();
    	if (netInfo == null){
    		Toast.makeText(this.getBaseContext(), getResources().getString(R.string.error_connection) + "\n" + getResources().getString(R.string.error_gps), Toast.LENGTH_LONG).show();
    	    return false;
    	}
    	if(netInfo.isConnectedOrConnecting() == false){
			Toast.makeText(this.getBaseContext(), getResources().getString(R.string.error_connection), Toast.LENGTH_LONG).show();
    	    return false;
    	}
    	return true;
    }
	
	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
	    private List<OverlayItem> items = new ArrayList<OverlayItem>();
	    private GeoPoint carPoint = null;
	    
	    public SitesOverlay(Drawable marker) {
			super(marker);
			boundCenterBottom(marker);
			updateCarPosition(true);
	    }
	    
	    @Override
	    protected OverlayItem createItem(int i) {
	    	return(items.get(i));
	    }
	    
	    @Override
	    protected boolean onTap(int i) {
	    	Toast.makeText(DondeActivity.this,
	                      items.get(i).getSnippet(),
	                      Toast.LENGTH_SHORT).show();
	    	return(true);
	    }
	    
	    @Override
	    public int size() {
	    	return(items.size());
	    }

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {	
			super.draw(canvas, mapView, shadow);
		} 
		
		public void updateCarPosition(boolean is_first){
			if (!is_first){
				items.remove(0);
			}
			carPoint = new GeoPoint((int)(carLocation.getLatitude()*1000000), (int)(carLocation.getLongitude()*1000000));
			items.add(new OverlayItem(carPoint,
					"Coche", 
					myShortCarTextInfo + myShortDistanceTextInfo));
			populate();
		}
		
	  }
	
	
	//Se extiende la clase MyLocationOverlay para sobreescribir el metodo drawCompass y poner nuestra propia brújula
	public class CurrentLocationOverlay extends MyLocationOverlay{

		private Bitmap pointer;
		public CurrentLocationOverlay(Context context, MapView mapView, Bitmap pointer) {
			super(context, mapView);
			this.pointer = pointer;
		}

		@Override
		protected boolean dispatchTap(){
			Toast.makeText(DondeActivity.this,
					myPositionTextInfo,
					Toast.LENGTH_LONG).show();
			return true;
		}

		@Override
		protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
			/*
		    float rotationAngle =  lastFix.getBearing() + 360f;
		    Matrix rotation = new Matrix();
		    rotation.preRotate(rotationAngle, pointer.getWidth()/2.0f, pointer.getHeight()/2.0f);
		    rotation.postTranslate(myLocation.getLongitudeE6() - pointer.getWidth()/2, myLocation.getLatitudeE6() - pointer.getHeight()/2);
		    canvas.drawBitmap(pointer, rotation, null);
		    */
			//Llamar si se quiere el punto azul de localización y el radio que indica la precisión
			super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
		}

		@Override
		protected void drawCompass(Canvas canvas, float bearing) {
			Point screenPts = new Point(0,0);
			//Trasladar mi GeoPoint a pixels de la pantalla
			if (myLoc != null){
				screenPts = mapView.getProjection().toPixels(
		    		new GeoPoint(
		    				(int) (myLoc.getLatitude()* 1000000),
		    				(int) (myLoc.getLongitude()* 1000000)
		    				),
		    		null);
			}
		    float rotationAngle = bearing + 360f;
		    Matrix rotation = new Matrix();
		    rotation.preRotate(rotationAngle, pointer.getWidth()/2.0f, pointer.getHeight()/2.0f);
		    rotation.postTranslate(screenPts.x - pointer.getWidth()/2, screenPts.y - pointer.getHeight()/2);
		    canvas.drawBitmap(pointer, rotation, null);
		    //Llamar si se quiere la imagen de la brújula por defecto:
		    //super.drawCompass(canvas, bearing);
		}
	}
}
