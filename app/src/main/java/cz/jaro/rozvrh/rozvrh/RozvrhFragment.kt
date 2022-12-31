package cz.jaro.rozvrh.rozvrh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = RepositoryImpl(requireContext())
        (view as ComposeView).setContent {

            if (repo.poprve) {
                val dm = isSystemInDarkTheme()

                LaunchedEffect(Unit) {
                    repo.poprve = false
                    repo.darkMode = dm
                }
            }

            AppTheme(repo.darkMode) {
                RozvrhScreen(repo)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as MainActivity).ukazatToolbar()
    }
}
