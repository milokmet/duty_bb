package com.symtoo.duty.preference;

import com.symtoo.duty.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {
	NumberPicker picker;
	public static final String XMLNS = "http://schemas.android.com/apk/res/android";
	Integer initialValue;
	
	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogMessage("popici");
		// defaultValue = attrs.getAttributeIntValue(NumberPickerPreference.XMLNS, "defaultValue");
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		picker = (NumberPicker) view.findViewById(R.id.prefNumberPicker);
		picker.setMaxValue(60);
		picker.setMinValue(1);
		if (this.initialValue != null ) {
			picker.setValue(this.initialValue);
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		int def = ( defaultValue instanceof Number ) ? (Integer)defaultValue
				: ( defaultValue != null ) ? Integer.parseInt(defaultValue.toString()) : 1;
		if ( restorePersistedValue ) {
			this.initialValue = getPersistedInt(def);
		} else {
			this.initialValue = (Integer)defaultValue;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if ( which == DialogInterface.BUTTON_POSITIVE ) {
			this.initialValue = picker.getValue();
			persistInt( initialValue );
			callChangeListener( initialValue );
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 1);
	}
}
