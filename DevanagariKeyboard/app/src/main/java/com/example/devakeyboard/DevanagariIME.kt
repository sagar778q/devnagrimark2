package com.example.devakeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View

class DevanagariIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var shift = false
    private var retro = false
    private var nukta = false
    private var lastConsonant = false

    data class Forms(
        val base: String,
        val long: String? = null,
        val retro: String? = null,
        val retroLong: String? = null,
        val nukta: String? = null,
        val nuktaLong: String? = null
    )
    data class VowelForm(val matra: String, val standalone: String)

    private val vowels = mapOf(
        'a' to VowelForm("ा", "आ"),
        'i' to VowelForm("ि", "इ"),
        'e' to VowelForm("ी", "ई"),
        'u' to VowelForm("ु", "उ"),
        'o' to VowelForm("ो", "ओ")
    )
    private val vowelsShift = mapOf(
        'a' to VowelForm("", "अ"),
        'i' to VowelForm("ै", "ऐ"),
        'e' to VowelForm("ॅ", "ऍ"),
        'u' to VowelForm("ू", "ऊ"),
        'o' to VowelForm("ौ", "औ")
    )
    private val rVowel = VowelForm("ृ", "ऋ")

    private val forms = mapOf(
        'q' to Forms("ङ"),
        'w' to Forms("ञ"),
        't' to Forms("त", long = "थ", retro = "ट", retroLong = "ठ"),
        'y' to Forms("य"),
        'p' to Forms("प", long = "फ"),
        's' to Forms("स", long = "श"),
        'd' to Forms("द", long = "ध", retro = "ड", retroLong = "ढ", nukta = "ड़", nuktaLong = "ढ़"),
        'f' to Forms("फ", nukta = "फ़"),
        'g' to Forms("ग", long = "घ"),
        'j' to Forms("ज", long = "झ", nukta = "ज़"),
        'k' to Forms("क", long = "ख"),
        'l' to Forms("ल", long = "ळ"),
        'z' to Forms("ज़"),
        'x' to Forms("क्ष", long = "ष"),
        'c' to Forms("च", long = "छ"),
        'v' to Forms("व"),
        'b' to Forms("ब", long = "भ"),
        'n' to Forms("न", retro = "ण"),
        'h' to Forms("ह"),
        'm' to Forms("म"),
        'r' to Forms("र")
    )

    private val digits = mapOf(
        48 to "०", 49 to "१", 50 to "२", 51 to "३", 52 to "४",
        53 to "५", 54 to "६", 55 to "७", 56 to "८", 57 to "९"
    )

    override fun onCreateInputView(): View {
        keyboard = Keyboard(this, R.xml.keyboard_devanagari)
        keyboardView = layoutInflater.inflate(R.layout.input, null) as KeyboardView
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)
        return keyboardView
    }

    private fun resolveConsonant(c: Char): String {
        val f = forms[c] ?: return c.toString()
        if (nukta && shift && f.nuktaLong != null) return f.nuktaLong
        if (nukta && f.nukta != null) return f.nukta
        if (retro && shift && f.retroLong != null) return f.retroLong
        if (shift && f.long != null) return f.long
        if (retro && f.retro != null) return f.retro
        return f.base
    }

    private fun commit(str: String, newLastConsonant: Boolean) {
        currentInputConnection?.commitText(str, 1)
        lastConsonant = newLastConsonant
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        keyboardView.post { syncStickyKeys() }

        when (primaryCode) {
            Keyboard.KEYCODE_SHIFT -> { shift = !shift }
            -101 -> { retro = !retro }
            -102 -> { nukta = !nukta }
            Keyboard.KEYCODE_DELETE -> { ic.deleteSurroundingText(1, 0); lastConsonant = false }
            -103 -> { ic.deleteSurroundingText(10000, 10000); lastConsonant = false }
            -104 -> {
                val out = if (shift) {
                    if (lastConsonant) "ॉ" else "ऑ"
                } else {
                    if (lastConsonant) "े" else "ए"
                }
                commit(out, false)
                shift = false
            }
            32 -> { commit(" ", false); retro = false }
            10 -> { commit("\n", false); retro = false }
            46 -> { commit(if (shift) "।" else "्", !shift); shift = false }
            in digits.keys -> { commit(digits[primaryCode]!!, false) }
            else -> {
                val ch = primaryCode.toChar().lowercaseChar()

                if (vowels.containsKey(ch)) {
                    val vf = if (shift) vowelsShift[ch]!! else vowels[ch]!!
                    commit(if (lastConsonant && vf.matra.isNotEmpty()) vf.matra else vf.standalone, false)
                    shift = false
                } else if (ch == 'r' && shift) {
                    commit(if (lastConsonant) rVowel.matra else rVowel.standalone, false)
                    shift = false
                } else if (ch == 'h' && shift) {
                    commit("ः", false); shift = false
                } else if (ch == 'm' && shift) {
                    commit("ं", false); shift = false
                } else {
                    val out = resolveConsonant(ch)
                    commit(out, true)
                    if (nukta) nukta = false
                    shift = false
                }
            }
        }
    }

    // Keeps Shift / ट-वर्ग / ़ highlighted only while they are actually active
    private fun syncStickyKeys() {
        keyboard.keys.forEach { k ->
            when (k.codes[0]) {
                Keyboard.KEYCODE_SHIFT -> k.on = shift
                -101 -> k.on = retro
                -102 -> k.on = nukta
            }
        }
        keyboardView.invalidateAllKeys()
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeUp() {}
    override fun swipeDown() {}
}
