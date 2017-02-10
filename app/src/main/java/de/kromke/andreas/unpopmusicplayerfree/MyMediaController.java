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

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;

/*
 * Slightly extend MediaController
 * Forwards key presses and implements no-autohide option
 */

public class MyMediaController extends MediaController
{
    private boolean mAutohideMode;

	public MyMediaController(Context c, boolean autohideMode)
    {
		super(c);
        mAutohideMode = autohideMode;
	}

    public void reallyHide()
    {
        Log.d("MyMediaController", "reallyHide()");
        if (isShowing())
        {
            Log.d("MyMediaController", "reallyHide() -- call super.hide()");
            super.hide();
        }
    }

    // pass keys for back, menu and search to activity, all others to base class
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int keyCode = event.getKeyCode();
        Log.d("MyMediaController", "dispatchKeyEvent() : keyCode = " + keyCode);
        //final boolean uniqueDown = (event.getRepeatCount() == 0) && (event.getAction() == KeyEvent.ACTION_DOWN);
        if ((keyCode == KeyEvent.KEYCODE_BACK) ||
            (keyCode == KeyEvent.KEYCODE_MENU) ||
            (keyCode == KeyEvent.KEYCODE_SEARCH))
        {
//            if (uniqueDown)
            {
                // pass key event to activity
                return ((Activity) getContext()).dispatchKeyEvent(event);
            }
//            return false;
        }
        else
        {
            return super.dispatchKeyEvent(event);
        }
    }

    public void hide()
	{
        Log.d("MyMediaController", "hide()");
        //suppress automatic hiding the controller, instead use explicit function above
        if (mAutohideMode)
        {
            super.hide();
        }
	}
}
