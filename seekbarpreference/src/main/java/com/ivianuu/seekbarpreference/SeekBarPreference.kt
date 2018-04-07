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
import java.util.*

class SeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = android.support.v7.preference.R.attr.seekBarPreferenceStyle
) : Preference(context, attrs, defStyle) {

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

    var format: String? = null
        set(value) {
            field = value
            notifyChanged()
        }

    private var defaultValue = 0
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

            format = a.getString(R.styleable.SeekBarPreference_format)

            a.recycle()
        }

        internalValue = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue)
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
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putInt(key, internalValue)
                        .apply()
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

        val format = format

        val text = if (format != null) {
            try {
                String.format(format, internalValue)
            } catch (e: IllegalFormatException) {
                internalValue.toString()
            }
        } else {
            internalValue.toString()
        }

        view.seekbar.progress = internalValue - min
        view.seekbar_value.text = text
    }

}