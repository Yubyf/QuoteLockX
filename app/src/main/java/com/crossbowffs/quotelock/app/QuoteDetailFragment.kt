package com.crossbowffs.quotelock.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.crossbowffs.quotelock.utils.TextResize
import com.crossbowffs.quotelock.utils.className
import com.yubyf.quotelockx.R


class QuoteDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_quote_detail, container, false).apply {
            findViewById<TextView>(R.id.tv_quote_text).apply {
                text = arguments?.getString(KEY_TEXT) ?: ""
                transitionName = arguments?.getString(KEY_TEXT_TRANSITION_NAME)
            }
            findViewById<TextView>(R.id.tv_quote_source).apply {
                text = arguments?.getString(KEY_SOURCE) ?: ""
                transitionName = arguments?.getString(KEY_SOURCE_TRANSITION_NAME)
            }
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(R.transition.shared_quote)
            (sharedElementEnterTransition as? TransitionSet)?.addTransition(TextResize())
            findViewById<TextView>(R.id.tv_quote_text).post {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        const val KEY_TEXT = "text"
        const val KEY_SOURCE = "source"
        const val KEY_TEXT_TRANSITION_NAME = "text_transition_name"
        const val KEY_SOURCE_TRANSITION_NAME = "source_transition_name"
        private val TAG = className<QuoteDetailFragment>()

        fun newInstance(
            text: String,
            source: String,
            textTransitionName: String,
            sourceTransitionName: String,
        ): QuoteDetailFragment {
            return QuoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TEXT, text)
                    putString(KEY_SOURCE, source)
                    putString(KEY_TEXT_TRANSITION_NAME, textTransitionName)
                    putString(KEY_SOURCE_TRANSITION_NAME, sourceTransitionName)
                }
            }
        }
    }
}