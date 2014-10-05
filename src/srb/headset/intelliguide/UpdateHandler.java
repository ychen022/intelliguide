package srb.headset.intelliguide;

import java.util.Locale;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.gn.intelligentheadset.IHS;
import com.gn.intelligentheadset.subsys.IHSAudio3DSound;
import com.google.android.gms.maps.model.LatLng;

public class UpdateHandler extends Handler {
	private int mInterval = 2000;
	private Runnable mUpdater;
	private MainActivity activity;
	private TextToSpeech mTts;
	private String voice;
	public IHSAudio3DSound m3DSound = null;

	public UpdateHandler(MainActivity act) {
		activity = act;
		final UpdateHandler self = this;
		mUpdater = new Runnable() {
			@Override
			public void run() {
				updateStatus();
				self.postDelayed(mUpdater, mInterval);
			}
		};
	}

	public void startTask() {
		mUpdater.run();
	}

	public void stopTask() {
		this.removeCallbacks(mUpdater);
	}

	// How does this have to be different from the normal loop?
	public void playAgain() {
		double minScore = 1000;
		GuidePortal minGP = null;

		LatLng curLL = activity.myLocation;
		// TODO Replace with direction from headset
		double curDir = activity.sensorPack.getCompassHeading();
		for (GuidePortal p : activity.guidePortals) {
			if (p.inRange(curLL, curDir)) {
				double gpDist = p.getDistance(curLL);
				double gpDir = p.getOrientation(curLL, curDir);
				double score = calculateHeuristic(gpDist, gpDir);
				if (score < minScore) {
					minScore = score;
					minGP = p;
				}
			}
		}
		if (minGP != null) {
			// TODO play sound
			m3DSound = activity.getIHS().createIHSAudio3DSound(IHS.SoundSource.ASSET, IHS.SoundFormat.WAVMONO16PCM,
                    new String[] {minGP.getPath() });
			m3DSound.setHeading((float) minGP.getPortalAngle(curLL));
			activity.mMyDevice.getAudio3DPlayer().addSound(m3DSound);
			activity.mMyDevice.getAudio3DPlayer().play();
			activity.changeInfoText(minGP.getName());
		}
	}

	public void getNearby() {
		LatLng curLL = activity.myLocation;
		double curDir = activity.sensorPack.getCompassHeading();
		int[] counts = new int[4];
		for (GuidePortal p : activity.guidePortals) {
			String dirString = p.inKM(curLL, curDir);
			if (dirString.equals("front")) {
				counts[0]++;
			} else if (dirString.equals("left")) {
				counts[1]++;
			} else if (dirString.equals("right")) {
				counts[2]++;
			} else if (dirString.equals("back")) {
				counts[3]++;
			}
		}
		StringBuilder sb = new StringBuilder();
		if (counts[0] != 0) {
			sb.append(counts[0]);
			sb.append(" points in the front. ");
		}
		if (counts[1] != 0) {
			sb.append(counts[1]);
			sb.append(" points on the left. ");
		}
		if (counts[2] != 0) {
			sb.append(counts[2]);
			sb.append(" points on the right. ");
		}
		if (counts[3] != 0) {
			sb.append(counts[3]);
			sb.append(" points in the back. ");
		}
		voice = sb.toString();
		if (voice.length()!=0){
			// Speak text in English
			mTts = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					if (status != TextToSpeech.ERROR) {
						mTts.setLanguage(Locale.US);
						mTts.speak(voice, TextToSpeech.QUEUE_FLUSH, null);
					}
				}
			});
		}
	}

	private double calculateHeuristic(double distance, double direction) {
		double distScore;
		if (distance < 0.1) {
			distScore = distance;
		} else if (distance < 0.3) {
			distScore = 3 * distance;
		} else if (distance < 0.5) {
			distScore = 7 * distance;
		} else if (distance < 0.7) {
			distScore = 11 * distance;
		} else {
			distScore = 16 * distance;
		}
		double directionScore = 0.05 * Math
				.abs(180 - Math.abs(180 - direction));
		return distScore + directionScore;
	}

	private void updateStatus() {
		double minScore = 1000;
		GuidePortal minGP = null;

		LatLng curLL = activity.myLocation;
		 // TODO Replace with direction from headset
		double curDir = activity.sensorPack.getCompassHeading();
		for (GuidePortal p : activity.guidePortals) {
			if (p.inRange(curLL, curDir)) {
				double gpDist = p.getDistance(curLL);
				double gpDir = p.getOrientation(curLL, curDir);
				double score = calculateHeuristic(gpDist, gpDir);
				if (score < minScore) {
					minScore = score;
					minGP = p;
				}
			}
		}

		if (minGP != null) {
			// TODO play sound
			m3DSound = activity.getIHS().createIHSAudio3DSound(IHS.SoundSource.ASSET, IHS.SoundFormat.WAVMONO16PCM,
                    new String[] {minGP.getPath() });
			m3DSound.setHeading((float) minGP.getPortalAngle(curLL)); 
			activity.mMyDevice.getAudio3DPlayer().addSound(m3DSound);
			activity.mMyDevice.getAudio3DPlayer().play();
			activity.changeInfoText(minGP.getName());
		}
	}
}
