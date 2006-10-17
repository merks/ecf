package org.jivesoftware.smackx.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.jingle.ContentInfo;

/**
 * Jingle content info
 * 
 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
 */
public class JingleContentInfo implements PacketExtension {

	protected ContentInfo mediaInfoElement;

	private String namespace;

	/**
	 * Empty constructor, with no media info.
	 */
	public JingleContentInfo() {
		this(null);
	}

	/**
	 * Constructor with a media info
	 * 
	 * @param mediaInfoElement MediaInfo element
	 */
	public JingleContentInfo(final ContentInfo mediaInfoElement) {
		super();
		this.mediaInfoElement = mediaInfoElement;
	}

	/**
	 * Get the media info element.
	 * 
	 * @return the mediaInfoElement
	 */
	public ContentInfo getMediaInfo() {
		return mediaInfoElement;
	}

	/**
	 * Get the element name
	 */
	public String getElementName() {
		// Media info is supposed to be just a single-word command...
		return getMediaInfo().toString();
	}

	/**
	 * Set the name space.
	 * 
	 * @param ns the namespace
	 */
	protected void setNamespace(final String ns) {
		namespace = ns;
	}

	/**
	 * Get the publilc namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	public String toXML() {
		StringBuffer buf = new StringBuffer();
		buf.append("<").append(getElementName()).append(" xmlns=\"");
		buf.append(getNamespace()).append("\" ");
		buf.append("/>");
		return buf.toString();
	}

	/**
	 * Transport part of a Jingle packet.
	 */
	public static class Audio extends JingleContentInfo {

		public static final String NAMESPACE = "http://jabber.org/protocol/jingle/info/audio";

		public Audio(final ContentInfo mi) {
			super(mi);
			setNamespace(NAMESPACE);
		}

		public String getNamespace() {
			return NAMESPACE;
		}

		// Subclasses: specialize the Audio media info...

		/**
		 * Busy media info.
		 */
		public static class Busy extends Audio {
			public Busy() {
				super(ContentInfo.Audio.BUSY);
			}
		}

		/**
		 * Hold media info.
		 */
		public static class Hold extends Audio {
			public Hold() {
				super(ContentInfo.Audio.HOLD);
			}
		}

		/**
		 * Mute media info.
		 */
		public static class Mute extends Audio {
			public Mute() {
				super(ContentInfo.Audio.MUTE);
			}
		}

		/**
		 * Queued media info.
		 */
		public static class Queued extends Audio {
			public Queued() {
				super(ContentInfo.Audio.QUEUED);
			}
		}

		/**
		 * Ringing media info.
		 */
		public static class Ringing extends Audio {
			public Ringing() {
				super(ContentInfo.Audio.RINGING);
			}
		}
	}
}
