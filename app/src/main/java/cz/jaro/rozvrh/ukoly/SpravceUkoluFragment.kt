package cz.jaro.rozvrh.ukoly

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import cz.jaro.rozvrh.R


class SpravceUkoluFragment : Fragment() {

    lateinit var adapter: UkolyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spravce_ukolu, container, false)

        adapter = UkolyAdapter(mutableListOf(), requireActivity())

        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val ll = LinearLayout(requireContext())
            val pb = ProgressBar(requireContext())
            pb.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            pb.updateLayoutParams<LinearLayout.LayoutParams> {
                setMargins(24, 24, 0, 24)
            }
            ll.addView(pb)

            setTitle(getString(R.string.nacitani))
            setView(ll)
            setCancelable(false)
        }.create()
        dialog.show()

        val database = Firebase.database("https://gymceska-b9b4c-default-rtdb.europe-west1.firebasedatabase.app/") // !!!
        val myRef = database.getReference("ukoly5")

        val rvUkoly = view.findViewById<RecyclerView>(R.id.rvUkoly)

        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        rvUkoly.layoutManager = llm

        rvUkoly.adapter = adapter

        var x = false

        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dialog.cancel()

                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return  // !!!!!

                if (x) return

                x = true

                val ukoly = dataSnapshot.getValue<MutableList<MutableMap<String, String>>>() ?: return

                adapter.seznam = ukoly

                adapter.notifyDataSetChanged()
                adapter.minulejSeznam = adapter.seznam
            }

            override fun onCancelled(error: DatabaseError) {

                dialog.cancel()

                Log.e("hm", "Hmmmm", error.toException())
            }
        })

        val fabPridat = view.findViewById<FloatingActionButton>(R.id.fabPridat)

        fabPridat.setOnClickListener {
            adapter.seznam += mutableMapOf(
                "datum" to "0. 0.",
                "predmet" to "",
                "nazev" to ""
            )
            adapter.ulozitAZaktualizovat()
        }
        return view
    }
}
