package com.example.coralmap.ui.carte

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.coralmap.R
import com.example.coralmap.databinding.ActivityMapsBinding
import com.example.coralmap.models.Utilisateur
import com.example.coralmap.models.UtilisateurLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Date

class MapsActivity : Fragment(), OnMapReadyCallback {

    private var markers = mutableMapOf<String, Marker>()
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private val handler = Handler()
    private val fetchUserLocationRunnable = object : Runnable {
        override fun run() {
            getLastKnownLocation()
            fetchAllUserLocationsFromFirebase()
            handler.postDelayed(this, 5000)
        }
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(


        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        return rootView
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val utilisateur = Utilisateur(
                        currentUser?.email ?: "",
                        currentUser?.uid ?: "",
                        currentUser?.displayName ?: "",
                        ""
                    )

                    val userLocation = UtilisateurLocation(
                        GeoPoint(location.latitude, location.longitude),
                        Date(),
                        utilisateur
                    )

                    saveUserLocationToFirebase(userLocation);
                }
            }
    }

    private fun saveUserLocationToFirebase(userLocation: UtilisateurLocation) {
        val db = FirebaseFirestore.getInstance()
        db.collection("userLocations")
            .document(userLocation.user.user_id)
            .set(userLocation)
            .addOnSuccessListener {
                Log.d(TAG, "User location saved successfully!")
                showUserLocationOnMap(userLocation)
            }
            .addOnFailureListener {
                Log.e(TAG, "Error saving user location", it)
            }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        } else {
            getLastKnownLocation()
            fetchAllUserLocationsFromFirebase()
        }
    }

    private fun fetchAllUserLocationsFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("userLocations")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userLocation = document.toObject(UtilisateurLocation::class.java)
                    updateMarkerForUser(userLocation)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user locations", exception)
            }
    }
    private fun showUserLocationOnMap(userLocation: UtilisateurLocation) {
        Log.d(TAG, userLocation.user.username);
        val userLatLng = LatLng(userLocation.geo_point.latitude, userLocation.geo_point.longitude)
        mMap.addMarker(MarkerOptions().position(userLatLng).title(userLocation.user.username))
    }

    private fun updateMarkerForUser(userLocation: UtilisateurLocation) {
        val userLatLng = LatLng(userLocation.geo_point.latitude, userLocation.geo_point.longitude)
        if (markers.containsKey(userLocation.user.user_id)) {
            markers[userLocation.user.user_id]?.position = userLatLng
        } else {
            val marker = mMap.addMarker(MarkerOptions().position(userLatLng).title(userLocation.user.username))
            markers[userLocation.user.user_id] = marker ?: return

        }
    }

    //Pour fetch toutes les 5 minutes
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.postDelayed(fetchUserLocationRunnable, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(fetchUserLocationRunnable)
    }
}
