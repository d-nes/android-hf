package hu.bme.aut.android.topkqh.destinationsharing

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.topkqh.destinationsharing.databinding.FragmentNewBinding


class NewFragment : DialogFragment() {

    private lateinit var binding: FragmentNewBinding

    override fun onCreateDialog(saveInstanceState: Bundle?): Dialog {
        binding = FragmentNewBinding.inflate(LayoutInflater.from(context))


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

                    if(address.size != 0) {
                        val intent = Intent(activity, HostMapsActivity::class.java)
                        intent.putExtra("Latitude", address[0].latitude.toString())
                        intent.putExtra("Longitude", address[0].longitude.toString())
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(requireContext(), "You need to provide a valid address", LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), "You need to provide an address", LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .create()
    }

    companion object {
        const val TAG = "NewItemDialogFragment"
    }

    private fun isValid() = binding.etDestination.text.isNotEmpty()





}