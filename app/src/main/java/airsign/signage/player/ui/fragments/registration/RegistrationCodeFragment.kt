package airsign.signage.player.ui.fragments.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import airsign.signage.player.R

@AndroidEntryPoint
class RegistrationCodeFragment : Fragment() {
    private lateinit var mRegistrationViewModel: RegistrationViewModel
    private lateinit var mQrcodeView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.registration_fragment, container, false)

        mRegistrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]


        mRegistrationViewModel.init(requireArguments().getString(CODE_KEY)!!)

        val mTextView = view.findViewById<TextView>(R.id.preview_display_code)

        mRegistrationViewModel.getCode().observe(viewLifecycleOwner) {
            mTextView.text = it
        }
        return view
    }

    companion object {
        private const val CODE_KEY = "MESSAGE_CODE_KEY"
        fun newInstance(message: String?): RegistrationCodeFragment {
            val bundle = Bundle()
            bundle.putString(CODE_KEY, message)
            val fragment = RegistrationCodeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}