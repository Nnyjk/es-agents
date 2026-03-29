package metrics

import (
	"net/http"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// Server Metrics HTTP 服务器
type Server struct {
	httpServer *http.Server
}

// NewServer 创建新的 metrics HTTP 服务器
func NewServer(addr string) *Server {
	mux := http.NewServeMux()

	// Prometheus metrics 端点
	mux.Handle("/metrics", promhttp.Handler())

	// 健康检查端点
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	})

	return &Server{
		httpServer: &http.Server{
			Addr:    addr,
			Handler: mux,
		},
	}
}

// Start 启动 HTTP 服务器（非阻塞）
func (s *Server) Start() error {
	go func() {
		if err := s.httpServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			// 记录错误，但不中断主程序
			// 这里可以添加日志记录
		}
	}()
	return nil
}

// Stop 停止 HTTP 服务器
func (s *Server) Stop() error {
	return s.httpServer.Close()
}
