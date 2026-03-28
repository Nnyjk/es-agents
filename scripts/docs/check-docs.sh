#!/bin/bash
#
# check-docs.sh - 文档一致性检查脚本
#
# 功能：
#   1. 检查 Markdown 文档中的内部链接是否有效
#   2. 检查术语使用是否一致
#   3. 输出检查报告
#
# 使用方式：
#   ./scripts/docs/check-docs.sh [docs-directory]
#
# 退出码：
#   0 - 所有检查通过
#   1 - 发现错误
#

set -e

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
DOCS_DIR="${1:-$PROJECT_ROOT/docs}"
TERMINOLOGY_FILE="$SCRIPT_DIR/terminology.txt"
LINK_CHECK_CONFIG="$SCRIPT_DIR/markdown-link-check-config.json"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 计数器
ERRORS=0
WARNINGS=0

echo "======================================"
echo "  ESA 文档一致性检查"
echo "======================================"
echo ""
echo "检查目录：$DOCS_DIR"
echo "术语表：$TERMINOLOGY_FILE"
echo ""

# ============================================
# 检查 1: Markdown 链接检查
# ============================================
echo "[$(date '+%H:%M:%S')] 检查 Markdown 链接..."

# 检查是否安装了 markdown-link-check
if ! command -v markdown-link-check &> /dev/null; then
    echo -e "${YELLOW}警告：markdown-link-check 未安装，跳过链接检查${NC}"
    echo "安装方法：npm install -g markdown-link-check"
    echo ""
else
    # 查找所有 Markdown 文件
    MARKDOWN_FILES=$(find "$DOCS_DIR" -name "*.md" -type f 2>/dev/null || true)
    
    if [ -z "$MARKDOWN_FILES" ]; then
        echo "  未找到 Markdown 文件"
    else
        LINK_ERRORS=0
        for file in $MARKDOWN_FILES; do
            # 只检查内部相对链接
            rel_path="${file#$PROJECT_ROOT/}"
            echo "  检查：$rel_path"
            
            # 运行链接检查（只检查相对链接）
            if ! markdown-link-check -c "$LINK_CHECK_CONFIG" "$file" 2>/dev/null; then
                LINK_ERRORS=$((LINK_ERRORS + 1))
            fi
        done
        
        if [ $LINK_ERRORS -gt 0 ]; then
            echo -e "${RED}  ✗ 发现 $LINK_ERRORS 个文件的链接有问题${NC}"
            ERRORS=$((ERRORS + LINK_ERRORS))
        else
            echo -e "${GREEN}  ✓ 所有链接检查通过${NC}"
        fi
    fi
fi

echo ""

# ============================================
# 检查 2: 术语一致性检查
# ============================================
echo "[$(date '+%H:%M:%S')] 检查术语一致性..."

if [ ! -f "$TERMINOLOGY_FILE" ]; then
    echo -e "${YELLOW}警告：术语表不存在，跳过术语检查${NC}"
else
    TERM_VIOLATIONS=0
    
    # 读取术语表（跳过注释和空行）
    while IFS='|' read -r standard wrong desc || [ -n "$standard" ]; do
        # 跳过注释和空行
        [[ "$standard" =~ ^#.*$ ]] && continue
        [[ -z "${standard// }" ]] && continue
        
        # 清理空白
        standard=$(echo "$standard" | xargs)
        wrong=$(echo "$wrong" | xargs)
        
        # 如果没有错误写法，跳过
        [[ -z "$wrong" ]] && continue
        
        # 分割错误写法（可能有多个）
        IFS=',' read -ra WRONG_TERMS <<< "$wrong"
        
        for wrong_term in "${WRONG_TERMS[@]}"; do
            wrong_term=$(echo "$wrong_term" | xargs)
            [[ -z "$wrong_term" ]] && continue
            
            # 在文档中搜索错误术语
            # 使用 grep 递归搜索，忽略大小写
            matches=$(grep -r -i -l "\b$wrong_term\b" "$DOCS_DIR" 2>/dev/null || true)
            
            if [ -n "$matches" ]; then
                echo -e "${YELLOW}  ! 发现术语 '$wrong_term' 应使用 '$standard'${NC}"
                for match in $matches; do
                    rel_path="${match#$PROJECT_ROOT/}"
                    echo "    - $rel_path"
                done
                TERM_VIOLATIONS=$((TERM_VIOLATIONS + 1))
            fi
        done
    done < "$TERMINOLOGY_FILE"
    
    if [ $TERM_VIOLATIONS -gt 0 ]; then
        echo -e "${YELLOW}  ~ 发现 $TERM_VIOLATIONS 处术语使用不一致${NC}"
        WARNINGS=$((WARNINGS + TERM_VIOLATIONS))
    else
        echo -e "${GREEN}  ✓ 术语使用一致${NC}"
    fi
fi

echo ""

# ============================================
# 检查 3: 文档结构检查
# ============================================
echo "[$(date '+%H:%M:%S')] 检查文档结构..."

STRUCTURE_ISSUES=0

# 检查 Markdown 文件是否有标题
for file in $(find "$DOCS_DIR" -name "*.md" -type f 2>/dev/null || true); do
    rel_path="${file#$PROJECT_ROOT/}"
    
    # 检查是否有至少一个标题
    if ! grep -q "^#" "$file" 2>/dev/null; then
        echo -e "${YELLOW}  ! $rel_path: 缺少标题${NC}"
        STRUCTURE_ISSUES=$((STRUCTURE_ISSUES + 1))
    fi
done

if [ $STRUCTURE_ISSUES -gt 0 ]; then
    echo -e "${YELLOW}  ~ 发现 $STRUCTURE_ISSUES 个文档结构问题${NC}"
    WARNINGS=$((WARNINGS + STRUCTURE_ISSUES))
else
    echo -e "${GREEN}  ✓ 文档结构正常${NC}"
fi

echo ""

# ============================================
# 总结
# ============================================
echo "======================================"
echo "  检查完成"
echo "======================================"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}错误：$ERRORS${NC}"
fi

if [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}警告：$WARNINGS${NC}"
fi

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有检查通过！${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ 无错误，有警告${NC}"
    exit 0
else
    echo -e "${RED}✗ 发现错误，请修复后重新提交${NC}"
    exit 1
fi
