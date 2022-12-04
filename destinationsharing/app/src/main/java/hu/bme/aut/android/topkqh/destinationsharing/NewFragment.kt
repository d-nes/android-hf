package hu.bme.aut.android.topkqh.destinationsharing

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import hu.bme.aut.android.topkqh.destinationsharing.databinding.FragmentNewBinding


class NewFragment : DialogFragment() {

    private lateinit var binding: FragmentNewBinding

    override fun onCreateDialog(saveInstanceState: Bundle?): Dialog {
        binding = FragmentNewBinding.inflate(LayoutInflater.from(context))

        //showPicker()

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_item)
            .setView(binding.root)
            .setPositiveButton(R.string.button_ok) {
                    dialogInterface, i ->
                if (isValid()) {
                    val address = Geocoder(this.context).getFromLocationName(
                        binding.etDestination.text.toString(),
                        1
                    )
                    val intent = Intent(activity, HostMapsActivity::class.java)
                    intent.putExtra("Latitude", address[0].latitude.toString())
                    intent.putExtra("Longitude", address[0].longitude.toString())
                    startActivity(intent)
                }else{
                    Toast.makeText(requireContext(), "You need to provide and address", LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .create()
    }

    companion object {
        const val TAG = "NewItemDialogFragment"
    }

    private fun isValid() = binding.etDestination.text.isNotEmpty()

/*
    //private val REQUEST_CODE = 1

    @Suppress("DEPRECATION")
    private fun showPicker() {
        Places.initialize(requireContext(), "AIzaSyBut5XGGiGMwGdNbGEAEe-8PhCB9IIUckE")
        val placesClient = Places.createClient(this.context)

        val fields = listOf(Place.Field.ID, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this.context)
        startActivityForResult(intent, REQUEST_CODE)
    }


    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage ?: "")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

 */




}