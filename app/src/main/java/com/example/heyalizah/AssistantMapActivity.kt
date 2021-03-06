package com.example.heyalizah

import android.Manifest
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
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
import com.google.firebase.database.*
import com.google.protobuf.DescriptorProtos

class AssistantMapActivity : AppCompatActivity(), OnMapReadyCallback,
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

    //Now we will get customer ID to assistant so can reach at his location
    lateinit var customerId : String
    lateinit var mCustomerMarker : Marker





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistant_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
        //Logout Button
        val mLoginout: Button = findViewById (R.id.logout_assistant);
        //Funtion to logout customer
        mLoginout.setOnClickListener(View.OnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            mDatabase = FirebaseDatabase.getInstance().getReference("AssistantAvailable")
            //referencing my database where i want my GeoFire data to be
            geofire = GeoFire(mDatabase)

            //removing the driver from driver avialable list because he is not longer available
            geofire!!.removeLocation(userId)
            FirebaseAuth.getInstance().signOut();
            startActivity(Intent(this,MainActivity::class.java))
            finish()

        })

        getAssinedCustomer();

    }
    //Getting Customer info to Assistant such as customer location and their Customer ID
    private fun getAssinedCustomer(){
        val assistantId  = FirebaseAuth.getInstance().currentUser?.uid
        var customerRef: DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Assistant").child(assistantId.toString()).child("customerRideId")
        customerRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                        customerId = dataSnapshot.getValue().toString()
                        //Here we will Assistant know where is customer location to go
                        getAssinedCustomerPickupLocation();
                    }
                }

        })
    }
    //Function implementation for getting customer info such as pick up location, their Customer ID
    private fun getAssinedCustomerPickupLocation(){
        //val assistantId = FirebaseAuth.getInstance().currentUser?.uid
        var assignedCustomerPickupLocation: DatabaseReference = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerId).child("l")
        var postlistner = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                if (p0.exists()) {
                    //Everythinng on list from wverything from DataSnapshot
                    var nMap = p0.getValue() as List<Any>
                    var locationLat = 0.0
                    var locationLng = 0.0

                    //Declaring varaible for assitant found
                    var mRequest: Button = findViewById(R.id.request);
                    mRequest.setText("Assistant Found")
                    if (nMap.get(0) != null) {
                        locationLat = nMap.get(0).toString().toDouble()
                        locationLng = nMap.get(1).toString().toDouble()

                    }
                    if (nMap.get(1) != null) {
                        locationLng = nMap.get(1).toString().toDouble()

                    }
                    //Now we will add Marker for Assitant to show customer where close by Assistant is Available
                    var assitantLatn = LatLng(locationLat, locationLng)
                    //we will remove the marker becuase we do not want so much marker all over the map once know the assistant location
                    //After showing the assistant location we will remove the marker
                    mCustomerMarker = map.addMarker(MarkerOptions().position(assitantLatn).title("Customer's Pickup Location"))
                }
            }
        }
            assignedCustomerPickupLocation.addValueEventListener(postlistner);
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
        //map.setOnMarkerClickListener(this)


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
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        //This is will draw a blue dot on Assistant current Location
                        map.isMyLocationEnabled = true

                        //map.addMarker(MarkerOptions().position(regina).title("My Favorite City"))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(regina, 12.0f))
                        Toast.makeText(this, location.latitude.toString() + location.longitude.toString(), Toast.LENGTH_LONG).show()

                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        //Here we are creating the table on datbase to see which assistant is available
                        mDatabase = FirebaseDatabase.getInstance().getReference("AssistantAvailable")
                        //referencing my database where i want my GeoFire data to be
                        geofire = GeoFire(mDatabase)

                        //setting assistant latitude and longitude in database
                        geofire!!.setLocation(userId,GeoLocation(clatitude,clongtidue))


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