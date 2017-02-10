/*
 * Copyright (C) 2016-17 Andreas Kromke, andreas.kromke@gmail.comn
 *
 * This program is free software; you can redistribute it or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.kromke.andreas.unpopmusicplayerfree;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * extends ListView with callback for click to unused space
 */
public class MyListView extends ListView
{

    private OnNoItemClickListener mOnNoItemClickListener;

    public interface OnNoItemClickListener
    {
        void onNoItemClicked();
    }

    public MyListView(Context context)
    {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        //check whether the touch hit any elements INCLUDING ListView footer
        if (pointToPosition((int) (ev.getX() * ev.getXPrecision()),
                (int) (ev.getY() * ev.getYPrecision())) == -1 && ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (mOnNoItemClickListener != null)
            {
                mOnNoItemClickListener.onNoItemClicked();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnNoItemClickListener(OnNoItemClickListener listener)
    {
        mOnNoItemClickListener = listener;
    }

}
