import React from "react";
import { Button, Tooltip } from "antd";
import { SunOutlined, MoonOutlined } from "@ant-design/icons";
import { useTheme } from "../contexts/ThemeContext";

// ============================================
// ESA 主题切换按钮 - 深色模式支持 (Issue #349)
// ============================================

interface ThemeToggleProps {
  size?: "small" | "middle" | "large";
}

export const ThemeToggle: React.FC<ThemeToggleProps> = ({
  size = "middle",
}) => {
  const { theme, toggleTheme } = useTheme();

  return (
    <Tooltip
      title={theme === "light" ? "切换到深色模式" : "切换到浅色模式"}
      placement="bottom"
    >
      <Button
        type="text"
        icon={theme === "light" ? <SunOutlined /> : <MoonOutlined />}
        onClick={toggleTheme}
        size={size}
        style={{
          borderRadius: "50%",
          transition: "transform 0.2s ease",
        }}
        aria-label="toggle theme"
      />
    </Tooltip>
  );
};

export default ThemeToggle;
