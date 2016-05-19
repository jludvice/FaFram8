package org.jboss.fuse.qa.fafram8.cluster.resolver;

/**
 * Enum of all resolvers.
 * Created by avano on 19.5.16.
 */
public enum Resolver {
	LOCALIP {
		@Override
		public String toString() {
			return "localip";
		}
	},
	LOCALHOSTNAME {
		@Override
		public String toString() {
			return "localhostname";
		}
	},
	PUBLICIP {
		@Override
		public String toString() {
			return "publicip";
		}
	},
	PUBLICHOSTNAME {
		@Override
		public String toString() {
			return "publichostname";
		}
	},
	MANUALIP {
		@Override
		public String toString() {
			return "manualip";
		}
	}
}
