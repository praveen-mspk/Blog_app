import React, { createContext, useContext, useEffect, useState } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SocketContext = createContext();

export const useSocket = () => useContext(SocketContext);

export const SocketProvider = ({ children }) => {
    const [stompClient, setStompClient] = useState(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        // Automatically uses standard environment variable or localhost fallback
        const apiUrl = process.env.REACT_APP_API_URL ? process.env.REACT_APP_API_URL.replace('/api/v1', '') : 'http://localhost:8080';
        const socket = new SockJS(`${apiUrl}/ws`);
        const client = Stomp.over(socket);
        
        client.debug = () => {}; // Disable debug logging for cleaner console
        
        client.connect({}, () => {
            setIsConnected(true);
            setStompClient(client);
        }, (error) => {
            console.error('STOMP Error:', error);
            setIsConnected(false);
        });

        return () => {
            if (client) {
                client.disconnect();
            }
        };
    }, []);

    const subscribeToCommunity = (communityId, callback) => {
        if (stompClient && isConnected) {
            return stompClient.subscribe(`/topic/community/${communityId}`, (message) => {
                callback(JSON.parse(message.body));
            });
        }
        return null;
    };

    const subscribeToDiscussion = (discussionId, callback) => {
        if (stompClient && isConnected) {
            return stompClient.subscribe(`/topic/discussion/${discussionId}`, (message) => {
                callback(JSON.parse(message.body));
            });
        }
        return null;
    };

    return (
        <SocketContext.Provider value={{ stompClient, isConnected, subscribeToCommunity, subscribeToDiscussion }}>
            {children}
        </SocketContext.Provider>
    );
};