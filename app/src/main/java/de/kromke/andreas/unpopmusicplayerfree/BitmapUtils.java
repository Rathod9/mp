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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * various useful functions
 */
class BitmapUtils
{
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight/*, ImageCache cache*/)
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        //addInBitmapOptions(options, cache);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }


    private static class BitmapCacheEntry
    {
        long last_access;
        Bitmap bitmap;
    }
    private static Map<Integer, BitmapCacheEntry> bitmapCache;
    private static int bitmapCacheLen;
    private static long bitmapCacheAccessTime;

    static void LruCacheCreate(int nEntries)
    {
        bitmapCache = new HashMap<>();
        bitmapCacheLen = nEntries;
        bitmapCacheAccessTime = 0;
    }

    static boolean LruCacheContainsKey(int key)
    {
        return bitmapCache.containsKey(key);
    }

    static Bitmap LruCacheGet(int key)
    {
        BitmapCacheEntry entry = bitmapCache.get(key);
        if (entry != null)
        {
            // update access time
            bitmapCacheAccessTime++;
            entry.last_access = bitmapCacheAccessTime;
            // return stored bitmap
            return entry.bitmap;
        }

        return null;
    }

    static void LruCachePut(int key, Bitmap bitmap)
    {
        BitmapCacheEntry entry = new BitmapCacheEntry();
        entry.last_access = bitmapCacheAccessTime;
        entry.bitmap = bitmap;
        if (bitmapCache.size() >= bitmapCacheLen)
        {
            // have to remove oldest element, i.e. that with lowest access time
            long oldestEntryAccessTime = Long.MAX_VALUE;
            int oldestEntryKey = -1;
            for (Map.Entry<Integer, BitmapCacheEntry> e : bitmapCache.entrySet())
            {
                int theKey = e.getKey();
                long theAccess = e.getValue().last_access;
                if ((oldestEntryKey < 0) || (theAccess < oldestEntryAccessTime))
                {
                    oldestEntryKey = theKey;
                    oldestEntryAccessTime = theAccess;
                }
            }
            bitmapCache.remove(oldestEntryKey);
        }
        bitmapCache.put(key, entry);
    }
}
