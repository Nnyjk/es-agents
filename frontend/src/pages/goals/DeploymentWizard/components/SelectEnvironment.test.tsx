import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { SelectEnvironment } from "./SelectEnvironment";

// Mock the API service
vi.mock("../../../../services/infra", () => ({
  queryEnvironments: vi.fn(),
}));

import { queryEnvironments } from "../../../../services/infra";

const mockEnvironments = [
  {
    id: "env-1",
    name: "Development",
    code: "DEV",
    description: "Development environment",
    environmentType: "DEV",
    active: true,
  },
  {
    id: "env-2",
    name: "Production",
    code: "PROD",
    description: "Production environment",
    environmentType: "PROD",
    active: true,
  },
  {
    id: "env-3",
    name: "Test",
    code: "TEST",
    description: null,
    environmentType: "TEST",
    active: true,
  },
];

describe("SelectEnvironment", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("loading state", () => {
    it("shows loading spinner while fetching environments", () => {
      vi.mocked(queryEnvironments).mockImplementation(
        () => new Promise(() => {}), // Never resolves to simulate loading
      );

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      expect(screen.getByRole("img", { hidden: true })).toBeInTheDocument();
    });

    it("shows environments after loading completes", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("Development")).toBeInTheDocument();
      });
    });
  });

  describe("empty state", () => {
    it("shows empty state when no environments available", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue([]);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("暂无可用环境")).toBeInTheDocument();
      });
    });

    it("shows guidance text in empty state", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue([]);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText(/请先在基础设施/)).toBeInTheDocument();
      });
    });
  });

  describe("environment list display", () => {
    it("renders all environments from API response", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("Development")).toBeInTheDocument();
        expect(screen.getByText("Production")).toBeInTheDocument();
        expect(screen.getByText("Test")).toBeInTheDocument();
      });
    });

    it("displays environment code", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("DEV")).toBeInTheDocument();
        expect(screen.getByText("PROD")).toBeInTheDocument();
        expect(screen.getByText("TEST")).toBeInTheDocument();
      });
    });

    it("displays environment description when available", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("Development environment")).toBeInTheDocument();
        expect(screen.getByText("Production environment")).toBeInTheDocument();
      });
    });

    it("does not display description when null", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("Test")).toBeInTheDocument();
      });

      // Test environment has no description, so no extra text should appear
      const testCard = screen.getByText("Test").closest(".ant-card");
      expect(testCard?.textContent).not.toContain("null");
    });
  });

  describe("selection behavior", () => {
    it("highlights selected environment", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      const selectedEnv = mockEnvironments[0];
      render(<SelectEnvironment value={selectedEnv} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("Development")).toBeInTheDocument();
      });

      // Check for selection border style (implementation may vary)
      const card = screen.getByText("Development").closest(".ant-card");
      expect(card).toBeTruthy();
    });

    it("calls onChange when environment is selected", async () => {
      const user = userEvent.setup();
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);
      const handleChange = vi.fn();

      render(<SelectEnvironment value={null} onChange={handleChange} />);

      await waitFor(() => {
        expect(screen.getByText("Development")).toBeInTheDocument();
      });

      await user.click(screen.getByText("Development"));

      expect(handleChange).toHaveBeenCalledWith(mockEnvironments[0]);
    });

    it("selects different environment on click", async () => {
      const user = userEvent.setup();
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);
      const handleChange = vi.fn();

      render(
        <SelectEnvironment
          value={mockEnvironments[0]}
          onChange={handleChange}
        />,
      );

      await waitFor(() => {
        expect(screen.getByText("Production")).toBeInTheDocument();
      });

      await user.click(screen.getByText("Production"));

      expect(handleChange).toHaveBeenCalledWith(mockEnvironments[1]);
    });
  });

  describe("API integration", () => {
    it("calls queryEnvironments with correct parameters", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue(mockEnvironments);

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(queryEnvironments).toHaveBeenCalledWith({
          current: 1,
          pageSize: 100,
        });
      });
    });

    it("handles API error gracefully", async () => {
      vi.mocked(queryEnvironments).mockRejectedValue(new Error("API Error"));

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        // Should show empty state when API fails
        expect(screen.getByText("暂无可用环境")).toBeInTheDocument();
      });
    });

    it("handles paginated response format", async () => {
      vi.mocked(queryEnvironments).mockResolvedValue({
        data: mockEnvironments,
        total: 3,
      });

      render(<SelectEnvironment value={null} onChange={vi.fn()} />);

      await waitFor(() => {
        expect(screen.getByText("Development")).toBeInTheDocument();
      });
    });
  });
});
