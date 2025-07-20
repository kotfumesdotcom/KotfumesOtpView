package com.kotfumes.otpinputview


import android.content.Context
import android.graphics.Color
import android.text.*
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.kotfumes.otpinputview.R

class OtpView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var otpLength: Int = 6
    private lateinit var hiddenEditText: EditText
    private val boxList = mutableListOf<TextView>()
    private var onOtpCompleteListener: ((String) -> Unit)? = null

    var allowPaste: Boolean = true
    private var boxBackgroundRes: Int = R.drawable.otp_box_background
    private var focusedBoxBackgroundRes: Int = R.drawable.otp_box_background_focused

    init {
        orientation = HORIZONTAL
        initAttributes(attrs)
        setupView()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.OtpView, 0, 0).apply {
            try {
                otpLength = getInteger(R.styleable.OtpView_otpLength, 6)
                boxBackgroundRes = getResourceId(R.styleable.OtpView_otpBoxBackground, R.drawable.otp_box_background)
                focusedBoxBackgroundRes = getResourceId(R.styleable.OtpView_otpBoxFocusedBackground, R.drawable.otp_box_background_focused)
            } finally {
                recycle()
            }
        }
    }

    private fun setupView() {
        hiddenEditText = EditText(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)
            isCursorVisible = false
            filters = arrayOf(InputFilter.LengthFilter(otpLength))
            inputType = InputType.TYPE_CLASS_NUMBER
            addTextChangedListener(otpTextWatcher)
            setOnLongClickListener {
                if (!allowPaste) {
                    Toast.makeText(context, "Pasting is disabled", Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
                false
            }
        }
        addView(hiddenEditText, LayoutParams(0, 0))

        for (i in 0 until otpLength) {
            val box = LayoutInflater.from(context).inflate(R.layout.otp_box, this, false) as TextView
            box.setOnClickListener { focusInput() }
            boxList.add(box)
            addView(box)
        }

        setOnClickListener { focusInput() }
    }

    private fun focusInput() {
        hiddenEditText.requestFocus()
        hiddenEditText.setSelection(hiddenEditText.text.length)
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(hiddenEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateOtpBoxes(text: Editable?) {
        val input = text.toString()
        for (i in 0 until otpLength) {
            if (i < input.length) {
                boxList[i].text = input[i].toString()
                boxList[i].setBackgroundResource(boxBackgroundRes)
            } else {
                boxList[i].text = ""
                if (i == input.length) {
                    boxList[i].setBackgroundResource(focusedBoxBackgroundRes)
                } else {
                    boxList[i].setBackgroundResource(boxBackgroundRes)
                }
            }
        }

        if (input.length == otpLength) {
            onOtpCompleteListener?.invoke(input)
        }
    }


    private val otpTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateOtpBoxes(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    fun setOtpCompleteListener(listener: (String) -> Unit) {
        this.onOtpCompleteListener = listener
    }

    fun getOtp(): String = hiddenEditText.text.toString()

    fun clearOtp() {
        hiddenEditText.setText("")
    }

    fun showError() {
        boxList.forEach { it.setBackgroundResource(R.drawable.otp_box_background_error) }
        shakeOtp()
    }

    fun clearError() {
        boxList.forEach { it.setBackgroundResource(boxBackgroundRes) }
    }

    fun shakeOtp() {
        val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
        boxList.forEach { it.startAnimation(shake) }
    }

    fun setBoxBackground(resId: Int) {
        boxBackgroundRes = resId
        refreshBoxes()
    }

    fun setFocusedBoxBackground(resId: Int) {
        focusedBoxBackgroundRes = resId
        refreshBoxes()
    }

    private fun refreshBoxes() {
        updateOtpBoxes(hiddenEditText.text)
    }


}