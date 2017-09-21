package com.jaimemartz.playerbalancer.connection;

import com.jaimemartz.playerbalancer.PlayerBalancer;
import com.jaimemartz.playerbalancer.ping.ServerStatus;
import com.jaimemartz.playerbalancer.section.ServerSection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum ProviderType {
    NONE {
        @Override
        public ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player) {
            return null;
        }
    },

    RANDOM {
        @Override
        public ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player) {
            return ProviderType.getRandom(servers);
        }
    },

    LOWEST {
        @Override
        public ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player) {
            int min = Integer.MAX_VALUE;
            ServerInfo target = null;

            for (ServerInfo server : servers) {
                int count = plugin.getNetworkManager().getPlayers(server);

                if (count < min) {
                    min = count;
                    target = server;
                }
            }

            return target;
        }
    },

    BALANCED {
        @Override
        public ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player) {
            List<ServerInfo> results = new ArrayList<>();
            int min = Integer.MAX_VALUE;

            for (ServerInfo server : servers) {
                int count = plugin.getNetworkManager().getPlayers(server);

                if (count <= min) {
                    if (count < min) {
                        min = count;
                        results.clear();
                    }
                    results.add(server);
                }
            }

            return ProviderType.getRandom(results);
        }
    },

    PROGRESSIVE {
        @Override
        public ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player) {
            for (ServerInfo server : servers) {
                ServerStatus status = plugin.getStatusManager().getStatus(server);
                if (plugin.getNetworkManager().getPlayers(server) < status.getMaximum()) {
                    return server;
                }
            }

            return ProviderType.getRandom(servers);
        }
    },

    FILLER {
        @Override
        public ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player) {
            int max = Integer.MIN_VALUE;
            ServerInfo target = null;

            for (ServerInfo server : servers) {
                ServerStatus status = plugin.getStatusManager().getStatus(server);
                int count = plugin.getNetworkManager().getPlayers(server);

                if (count > max && count <= status.getMaximum()) {
                    max = count;
                    target = server;
                }
            }

            return target;
        }
    };

    public abstract ServerInfo requestTarget(PlayerBalancer plugin, ServerSection section, List<ServerInfo> servers, ProxiedPlayer player);

    private static ServerInfo getRandom(List<ServerInfo> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}