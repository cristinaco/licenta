package ro.utcn.foodapp.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtil {
	public enum DirType {
		AUDIOS, PLANS, IMAGES
	}

	public static File getDrTempDir(final Context ctx) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) && ctx != null) {
			return ctx.getExternalFilesDir("images");
		} else {
			return null;
		}
	}

	public static File getDrDataDir(final Context ctx, DirType type) {
		return new File(getDrDataBaseDir(ctx), type.name().toLowerCase());
	}


	public static File getDrDataBaseDir(final Context ctx) {
		String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) && ctx != null) {
			return ctx.getExternalFilesDir("data");
		} else {
			return null;
		}
	}


	public static void copyFile(final File url, final File finalUrl) {
		finalUrl.getParentFile().mkdirs();
		url.renameTo(finalUrl);
	}


	public static boolean emptyAndDeleteDir(final File dir) {
		if (dir != null) {
			if (!dir.exists()) {
				return false;
			}

			if (!dir.isDirectory()) {
				dir.delete();
				return true;
			}

			final String[] files = dir.list();
			for (int i = 0, len = files.length; i < len; i++) {
				final File f = new File(dir, files[i]);
				if (f.isDirectory()) {
					emptyAndDeleteDir(f);
				} else {
					f.delete();
				}
			}
			return dir.delete();
		}
		return true;
	}
}