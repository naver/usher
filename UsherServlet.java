// 
// Copyright 2015 Naver
// Author : Dongseok Hyun <dustin.hyun@navercorp.com>
//
// This file is part of Usher.
//
// Usher is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Usher is distrubuted in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Usher. If not, see <http://www.gnu.org/licenses/>.

// UsherServlet.java
//
// Usher wrapper to be run as a servlet.
//
// TODO: Logging (Consider log4j)

import java.io.*;
import java.util.HashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.*;
import javax.servlet.http.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.lang.Math.*; // for min max

/**
 * Usher runs force directed layout algorithm and returns coordinates.
 * <p>
 * It gets node and edge data via HTTP request(JSON). Converting them into gml,
 * it runs Force Atlas layout algorithm with fixed amount of time. After all
 * iterations end, it responses with coordinate data as JSON format. Once
 * layout is done, it saves coordinates data into a file with hash from
 * request data as a file name so that same layout doesn't need to be
 * processed twice.
 *
 * 'message' has md5 hash value when 'status' is 'ok'. Hash value is
 * used as file name when coords data is saved.
 *
 * @author Dongseok Hyun
 */
public class UsherServlet extends HttpServlet {

	// Algorithm configurations
	private static final String cachePath = "../webapps/Usher/data/";

	public void init() throws ServletException {
	}

	// GET request handler
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{ 
		doPost(request, response);
	}

	// POST request handler
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();
		String dataString = request.getParameter("data");

		response.setContentType("text/html");

		if (dataString == null) {
			sendResponse(out, "error", "No data.", "");
			out.close();
			return;
		}

		Usher usher = new Usher();

		// Get hash for given data
		String hash = getMd5Hash(dataString);

		String fileName = "usher_" + hash + ".dat";
		File cacheFile = new File(cachePath + fileName);

		// Return from a file if cached.
		if (cacheFile.exists() && !cacheFile.isDirectory()) {
			try {
				sendResponse(
					out,
					"ok",
					hash,
					getFileContent(cachePath + fileName)
				);
				out.close();
				return;
			}
			catch (IOException ex) {
				// Will re-run layout algorithm returning exception
				// details via 'message' field.
				hash = ex.toString();
			}
		}

		// Otherwise, execute layout algorithm, cache the result and return.
		BufferedWriter cacheWriter =
			new BufferedWriter(new FileWriter(cachePath + fileName));

		String layoutDataString;

		try {
			layoutDataString = usher.getLayoutData(dataString);
		}
		catch (ParseException ex) {
			sendResponse(out, "error", ex.toString().replace("\"", "\\\""), "");
			return;
		}

		// Write to cache
		cacheWriter.write(layoutDataString);
		cacheWriter.close();

		// Response
		sendResponse(out, "ok", hash, layoutDataString);
		out.close();
	}

	public void destroy()
	{
		// Reserved
	}

	private void sendResponse(
		PrintWriter writer,
		String status,
		String message,
		String data)
	{
		String response = "{\"status\":\"" + status + "\",";
		response += "\"message\":\"" + message + "\",";
		response += "\"data\":" + data + "}";

		writer.println(response);
	}

	private String getMd5Hash(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());

			byte bytes[] = md.digest();
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < bytes.length; ++i) {
				sb.append(
					Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1)
				);
			}

			return sb.toString();
		}
		catch (NoSuchAlgorithmException ex) {
			// TODO: Logging exception, cached not available.
			return "NoHashAlgorithmSupported";
		}
	}

	private String getFileContent(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(
			new FileReader(fileName)
		);

		StringBuffer content = new StringBuffer();
		String line;

		while ((line = reader.readLine()) != null) {
			content.append(line);
			content.append("\n");
		}

		return content.toString();
	}
}
