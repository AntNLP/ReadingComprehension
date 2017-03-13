package Tools;

import java.util.HashMap;
import QABasedonDT.QABasedonDT;


public class Word2Vec {
	/***
	 *@author chenruili 
	 */
	HashMap<String, float[]> hashMap = new HashMap<>();

	public Word2Vec(HashMap<String, float[]> hashMap) {
		this.hashMap = hashMap;
	}

	public double getWordSimiliarity(String w1, String w2) {
		double dist = 0;
		float[] center = hashMap.get(w1);
		float[] center2 = hashMap.get(w2);
		if (center == null || center2 == null) {
			return 0;
		}
		for (int i = 0; i < center.length; i++) {
			dist += center[i] * center2[i];
		}
		return dist;

	}

	public float[] getSentenseSimiliarity(String str1, String str2) {
		float dist = 0;
		float dist2 = 0;
		String[] w1 = str1.split(" ");
		String[] w2 = str2.split(" ");
		float[] v1 = getAddVecCombination(w1);
		float[] v2 = getAddVecCombination(w2);

		float norm1 = getNorm(v1);
		float norm2 = getNorm(v2);
		for (int i = 0; i < v1.length; i++) {
			dist += v1[i] * v2[i];
		}
		if (norm1 * norm2 != 0) {
			dist = dist / (norm1 * norm2);
		}
		float[] _v1 = getMultiVecCombination(w1);
		float[] _v2 = getMultiVecCombination(w2);
		float _norm1 = getNorm(_v1);
		float _norm2 = getNorm(_v2);
		for (int i = 0; i < _v1.length; i++) {
			dist2 += _v1[i] * _v2[i];
		}
		if (_norm1 * _norm2 != 0) {
			dist2 = dist2 / (_norm1 * _norm2);
		}
		float dist3 = dist * dist2;
		float[] dist_array = { dist, dist2, dist3 };
		return dist_array;
	}

	public float getNorm(float[] vector) {
		float norm = 0;
		for (int i = 0; i < vector.length; i++) {
			norm += (vector[i] * vector[i]);
		}
		norm = (float) Math.sqrt(norm);
		return norm;
	}

	public float[] getMultiVecCombination(String[] w) {
		float[] v = new float[300];
		for (int i = 0; i < v.length; i++) {
			v[i] = 1;
		}
		for (int i = 0; i < w.length; i++) {
			try {
				float[] wv = hashMap.get(w[i]);
				for (int j = 0; j < v.length; j++) {
					v[j] = v[j] * wv[j];
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		if (w.length != 0) {
			for (int i = 0; i < v.length; i++) {
				v[i] = v[i] / (float) w.length;
			}
		}
		return v;
	}

	public float[] getAddVecCombination(String[] w) {
		float[] v = new float[300];
		for (int i = 0; i < w.length; i++) {
			try {
				float[] wv = hashMap.get(w[i]);
				for (int j = 0; j < v.length; j++) {
					v[j] = v[j] + wv[j];
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		if (w.length != 0) {
			for (int i = 0; i < v.length; i++) {
				v[i] = v[i] / (float) w.length;
			}
		}
		return v;
	}
}
