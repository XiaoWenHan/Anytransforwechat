package com.xwh.anytransforwechat.wxapi;

import java.io.File;
import java.util.Locale;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.GetMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.xwh.anytransforwechat.MainActivity;
import com.xwh.anytransforwechat.R;

public class WXEntryActivity extends ActionBarActivity implements
		IWXAPIEventHandler {

	private final int FILE_SELECT_REQCODE = 0x01;
	private IWXAPI iwxapi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		iwxapi = WXAPIFactory.createWXAPI(WXEntryActivity.this,
				MainActivity.APP_ID);
		iwxapi.handleIntent(getIntent(), this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			// 选取文件发送
			Intent intent_send = new Intent(Intent.ACTION_GET_CONTENT);
			intent_send.setType("*/*");
			intent_send.addCategory(Intent.CATEGORY_OPENABLE);
			try {
				startActivityForResult(
						Intent.createChooser(intent_send, getResources()
								.getString(R.string.upload_dialog_title)),
						FILE_SELECT_REQCODE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			// 打开文件
			try {
				WXAppExtendObject wxAppExtendObject = (WXAppExtendObject) ((((ShowMessageFromWX.Req) req).message.mediaObject));
				String filePath = wxAppExtendObject.filePath;
				String fileName = new JSONObject(wxAppExtendObject.extInfo)
						.getString("filename");
				Toast.makeText(WXEntryActivity.this, fileName,
						Toast.LENGTH_LONG).show();
				final File file = new File(filePath);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				String type = "*/*";
				String fName = file.getName();
				int dotIndex = fName.lastIndexOf(".");
				if (dotIndex < 0) {
					intent.setDataAndType(Uri.fromFile(file), "*/*");
				}
				String end;
				if (dotIndex > 0) {
					end = fName.substring(dotIndex, fName.length())
							.toLowerCase(Locale.getDefault());
				} else {
					end = "";
				}
				if (end.equals("")) {
					intent.setDataAndType(Uri.fromFile(file), "*/*");
				}
				for (int i = 0; i < MIME_MapTable.length; i++) {
					if (end.equals(MIME_MapTable[i][0]))
						type = MIME_MapTable[i][1];
				}
				intent.setDataAndType(Uri.fromFile(file), type);
				startActivity(intent);
				finish();
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FILE_SELECT_REQCODE:
			if (resultCode == RESULT_OK) {
				try {
					File sendFile = new File(getPath(getApplicationContext(),
							data.getData()));
					if (sendFile.length() * 1.0 / 1024 / 1024 > 8) {
						Toast.makeText(
								WXEntryActivity.this,
								getResources()
										.getString(R.string.send_toolarge),
								Toast.LENGTH_LONG).show();
					} else {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("filename", sendFile.getName());
						WXAppExtendObject wxAppExtendObject = new WXAppExtendObject();
						wxAppExtendObject.extInfo = jsonObject.toString();
						wxAppExtendObject.filePath = sendFile.getPath();
						WXMediaMessage msg = new WXMediaMessage();
						msg.mediaObject = wxAppExtendObject;
						SendMessageToWX.Req req = new SendMessageToWX.Req(
								getIntent().getExtras());
						GetMessageFromWX.Resp resp = new GetMessageFromWX.Resp();
						resp.transaction = req.transaction;
						resp.message = msg;
						iwxapi.sendResp(resp);
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;

		default:
			break;
		}
	}

	public final String[][] MIME_MapTable = { { ".3gp", "video/3gpp" },
			{ ".apk", "application/vnd.android.package-archive" },
			{ ".asf", "video/x-ms-asf" }, { ".avi", "video/x-msvideo" },
			{ ".bin", "application/octet-stream" }, { ".bmp", "image/bmp" },
			{ ".c", "text/plain" }, { ".class", "application/octet-stream" },
			{ ".conf", "text/plain" }, { ".cpp", "text/plain" },
			{ ".doc", "application/msword" },
			{ ".exe", "application/octet-stream" }, { ".gif", "image/gif" },
			{ ".gtar", "application/x-gtar" }, { ".gz", "application/x-gzip" },
			{ ".h", "text/plain" }, { ".htm", "text/html" },
			{ ".html", "text/html" }, { ".jar", "application/java-archive" },
			{ ".java", "text/plain" }, { ".jpeg", "image/jpeg" },
			{ ".jpg", "image/jpeg" }, { ".js", "application/x-javascript" },
			{ ".log", "text/plain" }, { ".m3u", "audio/x-mpegurl" },
			{ ".m4a", "audio/mp4a-latm" }, { ".m4b", "audio/mp4a-latm" },
			{ ".m4p", "audio/mp4a-latm" }, { ".m4u", "video/vnd.mpegurl" },
			{ ".m4v", "video/x-m4v" }, { ".mov", "video/quicktime" },
			{ ".mp2", "audio/x-mpeg" }, { ".mp3", "audio/x-mpeg" },
			{ ".mp4", "video/mp4" },
			{ ".mpc", "application/vnd.mpohun.certificate" },
			{ ".mpe", "video/mpeg" }, { ".mpeg", "video/mpeg" },
			{ ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" },
			{ ".mpga", "audio/mpeg" },
			{ ".msg", "application/vnd.ms-outlook" }, { ".ogg", "audio/ogg" },
			{ ".pdf", "application/pdf" }, { ".png", "image/png" },
			{ ".pps", "application/vnd.ms-powerpoint" },
			{ ".ppt", "application/vnd.ms-powerpoint" },
			{ ".prop", "text/plain" },
			{ ".rar", "application/x-rar-compressed" },
			{ ".rc", "text/plain" }, { ".rmvb", "audio/x-pn-realaudio" },
			{ ".rtf", "application/rtf" }, { ".sh", "text/plain" },
			{ ".tar", "application/x-tar" },
			{ ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
			{ ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
			{ ".wmv", "audio/x-ms-wmv" },
			{ ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
			{ ".z", "application/x-compress" }, { ".zip", "application/zip" },
			{ "", "*/*" } };

	// 从通用URI解析文件完整路径
	public static String getPath(Context context, Uri uri) {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
}
