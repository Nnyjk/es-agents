package plugin

import (
	"encoding/json"
	"strings"
)

const (
	// MagicPrefix is the prefix used to identify structured messages from Agents
	MagicPrefix = "::ES::"
)

// MessageType defines the type of the structured message
type MessageType string

const (
	MsgProgress MessageType = "progress"
	MsgResult   MessageType = "result"
	MsgError    MessageType = "error"
)

// MessagePayload represents the generic payload structure
// Actual payload depends on the type
type MessagePayload struct {
	Type    MessageType     `json:"type"`
	Payload json.RawMessage `json:"payload"`
}

// ProgressPayload represents a progress update
type ProgressPayload struct {
	Percent int    `json:"percent"`
	Msg     string `json:"msg"`
}

// ResultPayload represents the final result
type ResultPayload struct {
	Status string      `json:"status"`
	Data   interface{} `json:"data"`
}

// ErrorPayload represents an error
type ErrorPayload struct {
	Code int    `json:"code"`
	Msg  string `json:"msg"`
}

// ParseLine attempts to parse a line of output.
// Returns the payload if it's a structured message, or nil if it's a normal log line.
// The second return value is the raw log message (original line without prefix if parsed, or full line if not).
func ParseLine(line string) (*MessagePayload, string) {
	trimmed := strings.TrimSpace(line)
	if !strings.HasPrefix(trimmed, MagicPrefix) {
		return nil, line
	}

	jsonPart := strings.TrimPrefix(trimmed, MagicPrefix)
	var msg MessagePayload
	if err := json.Unmarshal([]byte(jsonPart), &msg); err != nil {
		// If JSON parsing fails, treat it as a normal log line but keep the prefix to indicate something was attempted
		return nil, line
	}

	return &msg, ""
}
