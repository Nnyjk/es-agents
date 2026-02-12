package transport

import (
	"fmt"
	"net/http"
	"sync"

	"github.com/gorilla/websocket"
)

type WSServer struct {
	SecretKey string
	Conn      *websocket.Conn
	mu        sync.Mutex
	// Callback for received messages
	OnMessage func([]byte)
}

func NewWSServer(secretKey string) *WSServer {
	return &WSServer{
		SecretKey: secretKey,
	}
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true // Allow all origins for now, or restrict if needed
	},
}

func (s *WSServer) Start(port int) error {
	http.HandleFunc("/ws", s.handleWebSocket)
	addr := fmt.Sprintf(":%d", port)
	fmt.Printf("Agent listening on %s\n", addr)
	return http.ListenAndServe(addr, nil)
}

func (s *WSServer) handleWebSocket(w http.ResponseWriter, r *http.Request) {
	// Verify Secret Key
	secret := r.Header.Get("X-Agent-Secret")
	if secret != s.SecretKey {
		// Also check query param as fallback if headers are stripped by some proxy
		if qSecret := r.URL.Query().Get("secret"); qSecret != s.SecretKey {
			http.Error(w, "Unauthorized", http.StatusUnauthorized)
			return
		}
	}

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		fmt.Printf("Upgrade failed: %v\n", err)
		return
	}

	s.mu.Lock()
	if s.Conn != nil {
		// Force close old connection if new one comes in?
		// Or reject? Let's close old one to allow reconnection.
		s.Conn.Close()
	}
	s.Conn = conn
	s.mu.Unlock()

	fmt.Printf("Server connected from %s via WebSocket\n", r.RemoteAddr)

	// Read loop
	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			fmt.Printf("Read error: %v\n", err)
			break
		}
		if s.OnMessage != nil {
			s.OnMessage(message)
		}
	}

	s.mu.Lock()
	if s.Conn == conn {
		s.Conn = nil
	}
	s.mu.Unlock()
	fmt.Println("Server disconnected")
}

func (s *WSServer) SendJSON(v interface{}) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.Conn == nil {
		return fmt.Errorf("not connected")
	}
	return s.Conn.WriteJSON(v)
}
