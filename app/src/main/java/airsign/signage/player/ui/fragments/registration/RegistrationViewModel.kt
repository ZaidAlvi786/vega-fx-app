package airsign.signage.player.ui.fragments.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel() {
    private val codeLiveData = MutableLiveData<String>()

    fun getCode(): LiveData<String> {
        return codeLiveData
    }

    fun init(code: String?) {
        codeLiveData.postValue(code)
    }

    private val TAG = "RegistrationViewModel"

}