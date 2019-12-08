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
import com.google.protobuf.Value
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap

class CustomerMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //we used random ID integar value so we will know if permission is granted than we will be changing the value
    val PERMISSION_ID = 1
    // finding radius variable
    var radius = 1.0;
    //Boolean if assitant found or not
    var aFound:Boolean = false
    var aFoundID: String = ""
    lateinit var mAssistantMarker : Marker


    //Declaring GeoFire
    var geofire : GeoFire? = null
    lateinit var mDatabase: DatabaseReference;
    var clongtidue = 1.0
    var clatitude = 1.0
    var pLocationLat = 1.0
    var pLocationLon = 1.0


    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //Assistant Location function to assitant location for customer
    private fun getAssistantLocation(){
        //here we are creating listener after list of available assistant is found and who is working with custoemrs
        //when Assistant is found we will move current assistant to working assistant table so he is no longer available
        // so he is not available to any other custimer until he is done with current customer
        var assistantRef: DatabaseReference = FirebaseDatabase.getInstance().getReference().child("AssistantWorking").child(aFoundID).child("l")


        // where L has latitude and longitude of assistant because this how firebase store data
        assistantRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.exists()) {
                    //Everythinng on list from wverything from DataSnapshot
                    var nMap = dataSnapshot.getValue() as List<Any>
                    var locationLat = 0.0
                    var locationLng = 0.0

                    //Declaring varaible for assitant found
                    val mRequest: Button = findViewById(R.id.request);
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
                    if (mAssistantMarker != null) {
                        mAssistantMarker.remove()

                    }
                    lateinit var loc1 : Location
                    loc1.latitude = pLocationLat
                    loc1.longitude = pLocationLon

                    lateinit var loc2 : Location
                    loc2.latitude = locationLat
                    loc2.longitude = locationLng

                    //finding the distance between two location customeer and Assistant
                    var distance = loc1.distanceTo(loc2)

                    mRequest.setText("Assistant Found: " + distance.toString())
                     mAssistantMarker =map.addMarker(MarkerOptions().position(assitantLatn).title("Your Assistant Location Who is coming"))
                }
            }


        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
        //Logout Button
        val mLoginout: Button = findViewById (R.id.logout_customer);
        //Funtion to logout customer
        mLoginout.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut();
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        })
        //Declaring varaible for customer request
        val mRequest: Button = findViewById (R.id.request);

        //Setting up Request button so custoemr can request Assistant
        mRequest.setOnClickListener(View.OnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid
            mDatabase = FirebaseDatabase.getInstance().getReference("CustomerRequest")
            //referencing my database where i want my GeoFire data to be
            geofire = GeoFire(mDatabase)

            //setting Customer latitude and longitude in database for pickup Request
            geofire!!.setLocation(uid,GeoLocation(clatitude,clongtidue))
            //Now will create marker for Pickup Location
            var pickupLocation = LatLng(clatitude,clongtidue)
            map.addMarker(MarkerOptions().position(pickupLocation).title("My Current Pickup Location"))
            pLocationLat = pickupLocation.latitude
            pLocationLon = pickupLocation.longitude
            // I am changing the text of Request Assistant to Requesting your Assistant. So the customer
            //knows that we searching for near by assistant for you
            mRequest.setText("Requesting your Assistant, Please Wait")

            //Calling function to get closer Assistant available to Customer
            getCloserAssistant();


        })

    }

    //Finding closer Assistant for Customer
    private fun getCloserAssistant(){

        var aLocation: DatabaseReference = FirebaseDatabase.getInstance().getReference().child("AssistantAvailable")
        var driverLocation = LatLng(clongtidue,clongtidue)
        var geofire:GeoFire = GeoFire(aLocation)
        var goQuery : GeoQuery = geofire.queryAtLocation(GeoLocation(driverLocation.latitude,driverLocation.longitude),radius)
        val gopostListner = object :GeoQueryEventListener{
            override fun onGeoQueryReady() {

                //when all available near assistant found within same radius we will call this function
                //we will see if assistant found within customer radius otherwise we will increase the radius to find more assistant
                if(!aFound){
                    radius++
                    getCloserAssistant();
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                //Anytime assistant found by  nearby radius this funtion will call and provide id of assistant and location
                // if nearby assistant found we will make this varaiable true
                if(!aFound) {
                    aFound = true
                    //Toast.makeText(this, "Assistant Found and He is on his Way", Toast.LENGTH_LONG).show()
                    if (key != null) {
                        aFoundID = key

                        var assistantRef: DatabaseReference =
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Assistant").child(aFoundID)
                        val uCustomerId = FirebaseAuth.getInstance().currentUser?.uid
                        val map: HashMap<String, Any>
                        uCustomerId?.let { map.put("CustomerRideId", it) }
                        assistantRef.updateChildren(map as Map<String, Any>)

                        //we will get driver location for customer
                        getAssistantLocation()
                        val mRequest: Button = findViewById (R.id.request);

                        mRequest.setText("Looking for Assistant Location")


                    };
                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onKeyExited(key: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }

        }

        goQuery.addGeoQueryEventListener(gopostListner)
        goQuery.removeAllListeners()


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
                        map.isMyLocationEnabled = true
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        //map.addMarker(MarkerOptions().position(regina).title("My Favorite City"))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(regina, 12.0f))
                        Toast.makeText(this, clatitude.toString() + clongtidue, Toast.LENGTH_LONG).show()

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

}