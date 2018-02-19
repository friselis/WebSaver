package Server;

import java.util.ArrayList;

/** Authentification service. */
public class SimpleAuthService implements AuthService {

        private static class Entry {

            private final String login;
            private final String password;
            private final String nickname;

            private Entry(String login, String password, String nickname) {
                this.login = login;
                this.password = password;
                this.nickname = nickname;
            }

            private boolean isMe(String login, String password) {
                return this.login.equals(login) && this.password.equals(password);
            }
        }

        private final ArrayList<Entry> entries = new ArrayList<>();

        @Override
        public void start() {
            entries.add(new Entry("svetlana", "123", "Светлана"));
        }

        @Override
        public String getNickname(String login, String password) {
            final int cnt = entries.size();
            for (int i = 0; i < cnt; i++) {
                Entry entry = entries.get(i);
                if(entry.isMe(login, password)) return entry.nickname;
            }
            return null;
        }

        @Override
        public void stop() {

        }
    }

