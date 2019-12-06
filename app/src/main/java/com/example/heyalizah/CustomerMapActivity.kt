package com.example.heyalizah

import android.Manifest
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.protobuf.DescriptorProtos

class CustomerMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //we used random ID integar value so we will know if permission is granted than we will be changing the value
    val PERMISSION_ID = 1

    //Declaring GeoFire
    var geofire : GeoFire? = null
    lateinit var mDatabase: DatabaseReference;
    var clongtidue = 1.0
    var clatitude = 1.0

    //Logout Button
    val mLoginout: Button = findViewById (R.id.logout_customer);


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        mLoginout.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut();
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        Toast.makeText(this, "onMapReady", Toast.LENGTH_LONG).show()
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.setOnMarkerClickListener(this)

        getLastLocation()


    }



    //This function will provide user last location and recoder user locations if there is any changes to it
    private fun getLastLocation() {



        if (checkPermissions()) {
            if (isLocationEnabled()) {
                Log.d("getLast","in")
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.d("getLastLoc",location.latitude.toString()+"  "+location.longitude.toString())
                        if(map == null)
                            Log.d("mapIsNull",location.longitude.toString()+"  "+location.longitude.toString())

                        clatitude = location.latitude
                        clongtidue= location.longitude


                        val regina = LatLng(clatitude,clongtidue)  // this is regina
                        map.addMarker(MarkerOptions().position(regina).title("My Favorite City"))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(regina, 12.0f))
                        Toast.makeText(this, location.latitude.toString() + location.longitude.toString(), Toast.LENGTH_LONG).show()

                    }
                }
            }else {
                Toast.makeText(this, "Allow your Location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }

    }

    // Here we will be checking rare cases when last location ==NULL which will record
    // location information in runtime
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
        }
    }

    //Now we will check if customers location is enabled or not. because user may grant the permission but location is turned off from their
    //phone settings
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }



    //We will check if app has granted the permission to use the location or not
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }
    // We are requesting permission from user
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }
    //we will check If permission is granted or denied
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onStop() {
        super.onStop()

    }

}