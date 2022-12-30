package cz.jaro.rozvrh.rozvrh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import cz.jaro.rozvrh.MainActivity
import cz.jaro.rozvrh.RepositoryImpl
import cz.jaro.rozvrh.ui.theme.AppTheme

class RozvrhFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): ComposeView {
        (requireActivity() as MainActivity).schovatToolbar()
        return ComposeView(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        (view as ComposeView).setContent { AppTheme { RozvrhScreen(RepositoryImpl(requireContext())) } }
    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as MainActivity).ukazatToolbar()
    }
}
