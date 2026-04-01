package airsign.signage.player.ui.fragments.message

import airsign.signage.player.BuildConfig
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
class MessageFragment : Fragment() {
    private lateinit var mMessageViewModel: MessageViewModel
    private lateinit var mConnectionImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.message_fragment, container, false)

        mMessageViewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        mConnectionImage = view.findViewById(R.id.connection_message)

        mMessageViewModel.init(requireArguments().getString(MESSAGE_KEY)!!)

        val mTextView = view.findViewById<TextView>(R.id.preview_display_message)
        val mPreviewDomain = view.findViewById<TextView>(R.id.preview_domain)

        mPreviewDomain.text = BuildConfig.DISPLAY_URL

        mMessageViewModel.getCode().observe(viewLifecycleOwner) {
            mTextView.text = it
        }

        return view
    }

    companion object {
        private const val MESSAGE_KEY = "MESSAGE_KEY"
        private const val CONNECTION_KEY = "CONNECTION_KEY"
        fun newInstance(message: String?, connected: Boolean): MessageFragment {
            val bundle = Bundle()
            bundle.putString(MESSAGE_KEY, message)
            bundle.putBoolean(CONNECTION_KEY, connected)
            val fragment = MessageFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}