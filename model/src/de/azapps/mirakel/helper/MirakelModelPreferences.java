package de.azapps.mirakel.helper;

import java.util.List;

import android.content.SharedPreferences.Editor;
import de.azapps.mirakel.model.R;
import de.azapps.mirakel.model.account.AccountMirakel;
import de.azapps.mirakel.model.account.AccountMirakel.ACCOUNT_TYPES;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.list.SpecialList;
import de.azapps.mirakel.model.task.Task;

public class MirakelModelPreferences extends MirakelPreferences {

	public static void setDefaultAccount(final AccountMirakel a) {
		settings.edit().putInt("defaultAccountID", a.getId()).commit();
	}

	public static AccountMirakel getDefaultAccount() {
		final int id = settings.getInt("defaultAccountID", AccountMirakel
				.getLocal().getId());
		final AccountMirakel a = AccountMirakel.get(id);
		if (a != null) {
			return a;
		}
		return AccountMirakel.getLocal();
	}

	public static ListMirakel getImportDefaultList(final boolean safe) {
		if (settings.getBoolean("importDefaultList", false)) {
			final int listId = settings.getInt("defaultImportList", 0);
			if (listId == 0) {
				return null;
			}
			return ListMirakel.getList(listId);
		}
		if (!safe) {
			return null;
		}
		return ListMirakel.safeFirst(context);
	}

	public static ListMirakel getListForSubtask(final Task parent) {
		ListMirakel list = null;
		if (settings.contains("subtaskAddToSameList")) {
			if (MirakelCommonPreferences.addSubtaskToSameList()) {
				list = parent.getList();
			} else {
				list = subtaskAddToList();
			}
		}
		// Create a new list and set this list as the default list for future
		// subtasks
		if (list == null) {
			list = ListMirakel.newList(context
					.getString(R.string.subtask_list_name));
		}
		return list;
	}

	private static ListMirakel getListFromIdString(final int preference) {
		ListMirakel list;
		try {
			list = ListMirakel.getList(preference);
		} catch (final NumberFormatException e) {
			list = SpecialList.firstSpecial();
		}
		if (list == null) {
			list = ListMirakel.safeFirst(context);
		}
		return list;
	}

	public static ListMirakel getNotificationsList() {
		return getListFromIdString(MirakelCommonPreferences
				.getNotificationsListId());
	}

	public static ListMirakel getNotificationsListOpen() {
		return ListMirakel.getList(MirakelCommonPreferences
				.getNotificationsListOpenId());
	}

	public static ListMirakel getStartupList() {
		try {
			return ListMirakel.safeGetList(Integer.parseInt(settings.getString(
					"startupList", "-1")));
		} catch (final NumberFormatException E) {
			return ListMirakel.safeFirst(context);
		}
	}

	public static int getSyncFrequency(final AccountMirakel account) {
		try {
			return Integer.parseInt(settings.getString("syncFrequency"
					+ account.getName(), "-1"));
		} catch (final NumberFormatException E) {
			return -1;
		}
	}

	public static boolean setSyncFrequency(final AccountMirakel account,
			final int minutes) {
		final Editor editor = getEditor();
		editor.putString("syncFrequency" + account.getName(), minutes + "");
		return editor.commit();
	}

	public static ListMirakel subtaskAddToList() {
		try {
			return ListMirakel.getList(settings.getInt("subtaskAddToList", -1));
		} catch (final Exception E) {
			// let old as fallback
			try {
				return ListMirakel.getList(Integer.parseInt(settings.getString(
						"subtaskAddToList", "-1")));
			} catch (final NumberFormatException e) {
				return null;
			}
		}
	}

	public static boolean useSync() {
		final List<AccountMirakel> all = AccountMirakel.getAll();
		for (final AccountMirakel a : all) {
			if (a.getType() != ACCOUNT_TYPES.LOCAL && a.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	public static String getDBName() {
		String db_name = "mirakel.db";
		if (MirakelCommonPreferences.isDemoMode()) {
			db_name = "demo_" + MirakelCommonPreferences.getLanguage() + ".db";
		}
		return db_name;
	}
}
