/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.seekbarpreference

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import kotlinx.android.synthetic.main.view_seekbar_preference.view.*

class SeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = android.support.v7.preference.R.attr.seekBarPreferenceStyle
) : Preference(context, attrs, defStyle) {

    var currentValue: Int
        set(value) {
            internalValue = value
            notifyChanged()
        }
        get() = internalValue

    var defaultValue = 0
        set(value) {
            field = value
            notifyChanged()
        }

    var min = 0
        set(value) {
            field = value
            notifyChanged()
        }

    var max = 100
        set(value) {
            field = value
            notifyChanged()
        }

    var incValue = 1
        set(value) {
            field = value
            notifyChanged()
        }

    var valueTextProvider: ValueTextProvider? = null
        set(value) {
            field = value
            notifyChanged()
        }

    private var internalValue = 0

    init {
        layoutResource = R.layout.view_seekbar_preference

        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference)

            min = a.getInt(R.styleable.SeekBarPreference_min, 0)
            max = a.getInt(R.styleable.SeekBarPreference_max, 100)
            incValue = a.getInt(R.styleable.SeekBarPreference_inc, 1)

            defaultValue = a.getInt(R.styleable.SeekBarPreference_android_defaultValue, 0)

            if (max <= min) {
                max = min + 1
            }

            if (incValue <= 0) {
                incValue = 1
            }

            val format = a.getString(R.styleable.SeekBarPreference_format)
            if (format != null) {
                valueTextProvider = StringFormatValueTextProvider(format)
            }


            a.recycle()
        }

        internalValue = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue)
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        val dataStore = preferenceDataStore
        internalValue = if (dataStore != null) {
            dataStore.getInt(key, defaultValue)
        } else if (preferenceManager != null) {
            preferenceManager.sharedPreferences.getInt(key, defaultValue)
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        with(holder.itemView) {
            seekbar.max = max - min
            seekbar.progress = internalValue - min

            seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) progressChanged(holder.itemView)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (callChangeListener(internalValue)) {
                        val dataStore = preferenceDataStore
                        if (dataStore != null) {
                            dataStore.putInt(key, internalValue)
                        } else {
                            val editor = preferenceManager?.sharedPreferences?.edit()
                            if (editor != null) {
                                editor.putInt(key, internalValue)
                                editor.apply()
                            }
                        }
                    }
                }
            })


            progressChanged(this)
        }

    }

    private fun progressChanged(view: View) {
        var progress = min + view.seekbar.progress

        if (progress < min) {
            progress = min
        }

        if (progress > max) {
            progress = max
        }

        internalValue = (Math.round((progress / incValue).toDouble()) * incValue).toInt()

        val provider = valueTextProvider

        val text = if (provider != null) {
            provider.getText(internalValue)
        } else {
            internalValue.toString()
        }

        view.seekbar.progress = internalValue - min
        view.seekbar_value.text = text
    }

    interface ValueChangeListener {

        fun onValueChanged(value: Int)

    }
}