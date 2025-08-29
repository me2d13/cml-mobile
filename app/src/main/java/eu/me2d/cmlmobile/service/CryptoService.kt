package eu.me2d.cmlmobile.service

import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import kotlin.io.encoding.Base64

class CryptoService {
    fun generateKeys(): KeyPairString {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val kp: KeyPair = kpg.generateKeyPair()
        val privateKey: PrivateKey? = kp.private
        val pub = kp.public
        val publicKey = Base64.encode(pub.encoded)
        val privateKeyString = Base64.encode(privateKey!!.encoded)
        Timber.d("Public key is %s", publicKey)
        //Timber.i("Private key is %s", Base64.encodeToString(privateKey!!.encoded, Base64.DEFAULT))
        return KeyPairString(publicKey, privateKeyString)
    }
}

data class KeyPairString(val publicKey: String, val privateKey: String)