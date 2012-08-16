/*
 * Copyright (C) 2011, 2012 Riccardo Massera, r.massera@thecoder4.eu
 * 
 * This file is part of PleftDroid.
 * 
 * PleftDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PleftDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PleftDroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.thecoder4.gpl.pleftdroid;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DateVoteBarView extends View {
	private Paint mTextPaint;
	private String mText;
	private int mAscent;
	private int mWidth;
	private int mHeight;
	private static final int BAR_HEIGHT = 10;
	private int mVoters;
	private ArrayList<String> mVotes;
	/**
	 * Constructor.  This version is only needed if you will be instantiating
	 * the object manually (not from a layout XML file).
	 * @param context
	 */
	public DateVoteBarView(Context context) {
		super(context);
		initLabelView();
	}

	/**
	 * Construct object, initializing with any attributes we understand from a
	 * layout file. These attributes are defined in
	 * SDK/assets/res/any/classes.xml.
	 * 
	 * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
	 */
	public DateVoteBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLabelView();

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DateVoteBarView);

		CharSequence s = a.getString(R.styleable.DateVoteBarView_text);
		if (s != null) {
			setText(s.toString());
		}

		// Retrieve the color(s) to be used for this view and apply them.
		// Note, if you only care about supporting a single color, that you
		// can instead call a.getColor() and pass that to setTextColor().
		setTextColor(a.getColor(R.styleable.DateVoteBarView_textColor, 0xFF000000));

		int textSize = a.getDimensionPixelOffset(R.styleable.DateVoteBarView_textSize, 0);
		if (textSize > 0) {
			setTextSize(textSize);
		}

		a.recycle();
	}

	private final void initLabelView() {
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(26);
		mTextPaint.setColor(Color.WHITE);

		setPadding(3, 3, 3, 3);
	}

	/**
	 * Sets the text to display in this label
	 * @param text The text to display. This will be drawn as one line.
	 */
	public void setText(String text) {
		mText = text;
		requestLayout();
		invalidate();
	}

	/**
	 * Sets the text size for this label
	 * @param size Font size
	 */
	public void setTextSize(int size) {
		mTextPaint.setTextSize(size);
		requestLayout();
		invalidate();
	}

	/**
	 * Sets the text color for this label.
	 * @param color ARGB value for the text
	 */
	public void setTextColor(int color) {
		mTextPaint.setColor(color);
		invalidate();
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}

	/**
	 * Determines the width of this view
	 * @param measureSpec A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
			+ getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by measureSpec
				result = Math.min(result, specSize);
			}
		}
		mWidth=result;

		return result;
	}

	/**
	 * Determines the height of this view
	 * @param measureSpec A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		mAscent = (int) mTextPaint.ascent();
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text (beware: ascent is a negative number)
			result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop() + getPaddingTop() + BAR_HEIGHT
			+ getPaddingBottom();
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by measureSpec
				result = Math.min(result, specSize);
			}
		}
		mHeight=result;
		return result;
	}

	/**
	 * Render the text
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int rw = (mWidth-getPaddingLeft()-getPaddingRight())/mVoters;
		int th = (int) (-mAscent + mTextPaint.descent());

		if(AppPreferences.INSTANCE.getUsePleftTheme()) { mTextPaint.setColor(Color.DKGRAY); }
		else                                           { mTextPaint.setColor(Color.WHITE); }
		canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.RED);
		for(int n=0; n<mVoters; n++) {

			// 0=? 1=OK -1=KO
            if(mVotes.get(n).equals("-1")) {
            	paint.setColor(Color.RED);
            } else if(mVotes.get(n).equals("1")) {
            	if(AppPreferences.INSTANCE.getUsePleftTheme()) { paint.setColor(0xff00cc00); }
            	else                                           { paint.setColor(Color.GREEN); }
            } else {
            	paint.setColor(Color.YELLOW);
            }

			canvas.drawRoundRect(new RectF(getPaddingLeft()+(n*rw), th+getPaddingTop(), rw+(n*rw), mHeight), 3, 3, paint);
		}

	}

	public void setmVotes(ArrayList<String> mVotes) {
		if(mVotes.size()>0) {
			this.mVotes = mVotes;
			this.mVoters = mVotes.size();
		}
	}

}
