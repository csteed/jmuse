import java.io.*;
import java.util.*;

public class DataFile implements Runnable {
	private File file;
	private ArrayList names;
	private ArrayList data;
	private ArrayList listeners = new ArrayList();

	public DataFile(File f) {
		file = f;
	}

	public void addDataFileListener(DataFileListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeDataFileListener(DataFileListener listener) {
		listeners.remove(listener);
	}

	public void run() {
		try {
			Iterator iter = listeners.iterator();
			while (iter.hasNext()) {
				DataFileListener listener = (DataFileListener) iter.next();
				listener.dataFileReadStarted();
			}

			read();

			iter = listeners.iterator();
			while (iter.hasNext()) {
				DataFileListener listener = (DataFileListener) iter.next();
				listener.dataFileReadFinished(this);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void read() throws IOException {
		int line_counter = 0;
		int skipped_records = 0;
		boolean skip_record = false;

		names = new ArrayList();
		data = new ArrayList();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;

		while ((line = in.readLine()) != null) {
			// System.out.println("line no. " + line_counter);
			skip_record = false;
			if (line_counter == 0) {
				String tokens[] = line.split(",");
				if (tokens != null) {
					for (int i = 0; i < tokens.length; i++) {
						names.add(tokens[i].trim());
						data.add(new ArrayList());
					}
				}
				/*
				 * StringTokenizer st = new StringTokenizer(line); while
				 * (st.hasMoreTokens()) { String token = st.nextToken().trim();
				 * if (token.endsWith(",")) { token = token.substring(0,
				 * token.length()-1); } names.add(token); data.add(new
				 * ArrayList()); }
				 */
			} else {
				ArrayList rowList = new ArrayList();
				String tokens[] = line.split(",");
				if (tokens != null) {
					for (int i = 0; i < tokens.length; i++) {
						try {
							rowList.add(Float.parseFloat(tokens[i].trim()));
						} catch (Exception ex) {
							System.out.println("DataFile.read(): "
									+ ex.toString() + ". Skipping record");
							skip_record = true;
							skipped_records++;
							break;
						}
					}

					if (!skip_record) {
						for (int i = 0; i < rowList.size(); i++) {
							ArrayList varList = (ArrayList) data.get(i);
							varList.add(rowList.get(i));
						}
					}
				}

				/*
				 * StringTokenizer st = new StringTokenizer(line);
				 * while(st.hasMoreTokens() && !skip_record) { String token =
				 * st.nextToken().trim(); if (token.endsWith(",")) { token =
				 * token.substring(0, token.length()-1); }
				 * 
				 * try { rowList.add(Float.parseFloat(token)); } catch
				 * (Exception ex) { System.out.println("DataFile.read(): " +
				 * ex.toString()); skip_record = true; skipped_records++; } }
				 * 
				 * if (!skip_record) { for (int i = 0; i < rowList.size(); i++)
				 * { ArrayList varList = (ArrayList)data.get(i);
				 * varList.add(rowList.get(i)); } }
				 */
			}
			line_counter++;
		}
		System.out.println("num_skipped records is " + skipped_records);
	}

	public ArrayList getVariableNames() {
		return names;
	}

	public String getVariableName(int idx) {
		return (String) names.get(idx);
	}

	public int getVariableCount() {
		return names.size();
	}

	public int getRecordCount() {
		if (data.size() > 0) {
			return ((ArrayList) data.get(0)).size();
		}
		return 0;
	}

	public ArrayList getVariableValues(int idx) {
		if (idx <= data.size()) {
			return (ArrayList) data.get(idx);
		}
		return null;
	}

	public static void main(String args[]) throws Exception {
		// DataFile df = new DataFile(new File("data/cars.txt"));
		DataFile df = new DataFile(new File(
				"data/hurricane_season_forecast.txt"));
		df.read();

		System.out.println("Number of variables is " + df.getVariableCount());
		System.out.println("Number of records is " + df.getRecordCount());

		for (int i = 0; i < df.getVariableCount(); i++) {
			System.out.print("'" + df.getVariableName(i) + "':");
		}
		System.out.print("\n");
	}
}
