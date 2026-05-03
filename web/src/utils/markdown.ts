import markdownit from 'markdown-it'
import type MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const md: MarkdownIt = markdownit({
  html: true,
  xhtmlOut: true,
  breaks: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        const highlighted = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
        return `<pre class="hljs"><code class="language-${lang}">${highlighted}</code></pre>`
      } catch {
        // fallback
      }
    }
    return `<pre class="hljs"><code>${escapeHtml(str)}</code></pre>`
  }
})

md.linkify.set({ fuzzyLink: true })

const defaultRender = md.renderer.rules.link_open || function(tokens, idx, options, _env, self) {
  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const aIndex = tokens[idx].attrIndex('target')
  if (aIndex < 0) {
    tokens[idx].attrPush(['target', '_blank'])
  } else {
    tokens[idx].attrs![aIndex][1] = '_blank'
  }
  
  const relIndex = tokens[idx].attrIndex('rel')
  if (relIndex < 0) {
    tokens[idx].attrPush(['rel', 'noopener noreferrer'])
  } else {
    tokens[idx].attrs![relIndex][1] = 'noopener noreferrer'
  }
  
  return defaultRender(tokens, idx, options, env, self)
}

export function escapeHtml(text: string): string {
  const htmlEntities: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }
  
  return text.replace(/[&<>"']/g, char => htmlEntities[char] || char)
}

export function parseMarkdown(text: string): string {
  if (!text || typeof text !== 'string') {
    return ''
  }
  
  const processed = preprocessMarkdownForRendering(text)
  const sanitized = sanitizeMarkdownInput(processed)
  return md.render(sanitized)
}

function sanitizeMarkdownInput(input: string): string {
  let sanitized = input
  
  const dangerousPatterns = [
    /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
    /javascript\s*:/gi,
    /vbscript\s*:/gi,
    /data\s*:\s*text\/html/gi,
    /on\w+\s*=/gi
  ]
  
  for (const pattern of dangerousPatterns) {
    sanitized = sanitized.replace(pattern, '')
  }
  
  return sanitized
}

export function normalizeMarkdown(text: string): string {
  if (!text) return ''
  
  let normalized = text
  
  normalized = normalized.replace(/&amp;/g, '&')
  normalized = normalized.replace(/&lt;/g, '<')
  normalized = normalized.replace(/&gt;/g, '>')
  normalized = normalized.replace(/&quot;/g, '"')
  normalized = normalized.replace(/&#39;/g, "'")
  
  normalized = normalized.replace(/[\u2018\u2019]/g, "'")
  normalized = normalized.replace(/[\u201C\u201D]/g, '"')
  normalized = normalized.replace(/[\u2013\u2014]/g, '-')
  
  normalized = normalized.replace(/\\\\`/g, '`')
  normalized = normalized.replace(/\\`/g, '`')
  
  normalized = normalized.replace(/\u00a0/g, ' ')
  
  normalized = normalized.replace(/\r\n/g, '\n')
  normalized = normalized.replace(/\r/g, '\n')
  
  normalized = normalized.replace(/`{4,}/gm, '```')
  
  normalized = normalized.replace(/\n{3,}/g, '\n\n')
  
  return normalized
}

/**
 * 预处理 Markdown，仅用于预览渲染，不影响编辑区域
 * 支持从各种来源复制的 Markdown 内容
 */
function preprocessMarkdownForRendering(text: string): string {
  if (!text) return ''
  
  let processed = text
  
  // HTML 实体解码 - 处理复制过来的转义字符
  processed = processed.replace(/&amp;/g, '&')
  processed = processed.replace(/&lt;/g, '<')
  processed = processed.replace(/&gt;/g, '>')
  processed = processed.replace(/&quot;/g, '"')
  processed = processed.replace(/&#39;/g, "'")
  processed = processed.replace(/&nbsp;/g, ' ')
  processed = processed.replace(/&copy;/g, '©')
  processed = processed.replace(/&reg;/g, '®')
  processed = processed.replace(/&trade;/g, '™')
  
  // Unicode 特殊字符处理 - 处理从 Word/网页复制的特殊字符
  processed = processed.replace(/[\u2018\u2019\u201A\u201B]/g, "'")
  processed = processed.replace(/[\u201C\u201D\u201E\u201F]/g, '"')
  processed = processed.replace(/[\u2013\u2014\u2015]/g, '-')
  processed = processed.replace(/[\u2026]/g, '...')
  processed = processed.replace(/[\u00AB\u00BB]/g, '"')
  
  // 处理转义字符 - 处理各种转义的反引号
  processed = processed.replace(/\\\\\\\`/g, '`')
  processed = processed.replace(/\\\\`/g, '`')
  processed = processed.replace(/\\\`/g, '`')
  processed = processed.replace(/\\"/g, '"')
  processed = processed.replace(/\\'/g, "'")
  
  // 处理特殊空格字符
  processed = processed.replace(/\u00a0/g, ' ')
  processed = processed.replace(/\u2002/g, ' ')
  processed = processed.replace(/\u2003/g, ' ')
  processed = processed.replace(/\u2004/g, ' ')
  processed = processed.replace(/\u2005/g, ' ')
  processed = processed.replace(/\u2006/g, ' ')
  processed = processed.replace(/\u2007/g, ' ')
  processed = processed.replace(/\u2008/g, ' ')
  processed = processed.replace(/\u2009/g, ' ')
  processed = processed.replace(/\u200A/g, ' ')
  processed = processed.replace(/\u200B/g, '')
  
  // 制表符转换为空格
  processed = processed.replace(/\t/g, '    ')
  
  // 统一换行符
  processed = processed.replace(/\r\n/g, '\n')
  processed = processed.replace(/\r/g, '\n')
  
  // 处理代码块标记，确保格式正确
  processed = processed.replace(/`{4,}/gm, '```')
  
  // 清理连续空行
  processed = processed.replace(/\n{3,}/g, '\n\n')
  
  return processed
}

export function htmlToMarkdown(html: string): string {
  if (!html) return ''
  
  const text = html
    .replace(/<h1[^>]*>(.*?)<\/h1>/gi, '# $1\n\n')
    .replace(/<h2[^>]*>(.*?)<\/h2>/gi, '## $1\n\n')
    .replace(/<h3[^>]*>(.*?)<\/h3>/gi, '### $1\n\n')
    .replace(/<h4[^>]*>(.*?)<\/h4>/gi, '#### $1\n\n')
    .replace(/<h5[^>]*>(.*?)<\/h5>/gi, '##### $1\n\n')
    .replace(/<h6[^>]*>(.*?)<\/h6>/gi, '###### $1\n\n')
    .replace(/<strong[^>]*>(.*?)<\/strong>/gi, '**$1**')
    .replace(/<b[^>]*>(.*?)<\/b>/gi, '**$1**')
    .replace(/<em[^>]*>(.*?)<\/em>/gi, '*$1*')
    .replace(/<i[^>]*>(.*?)<\/i>/gi, '*$1*')
    .replace(/<del[^>]*>(.*?)<\/del>/gi, '~~$1~~')
    .replace(/<s[^>]*>(.*?)<\/s>/gi, '~~$1~~')
    .replace(/<code[^>]*>(.*?)<\/code>/gi, '`$1`')
    .replace(/<pre[^>]*><code[^>]*>(.*?)<\/code><\/pre>/gis, '```\n$1\n```\n\n')
    .replace(/<p[^>]*>(.*?)<\/p>/gis, '$1\n\n')
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<li[^>]*>(.*?)<\/li>/gi, '- $1\n')
    .replace(/<\/?ul[^>]*>/gi, '')
    .replace(/<\/?ol[^>]*>/gi, '')
    .replace(/<blockquote[^>]*>(.*?)<\/blockquote>/gis, (_, content) => {
      return content.split('\n').map((line: string) => `> ${line}`).join('\n') + '\n\n'
    })
    .replace(/<a[^>]*href="([^"]+)"[^>]*>(.*?)<\/a>/gi, '[$2]($1)')
    .replace(/<img[^>]*src="([^"]+)"[^>]*alt="([^"]*)"[^>]*\/?>/gi, '![$2]($1)')
    .replace(/<img[^>]*src="([^"]+)"[^>]*\/?>/gi, '![]($1)')
    .replace(/<hr\s*\/?>/gi, '\n---\n\n')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\n{3,}/g, '\n\n')
    .trim()
  
  return text
}

export const MarkdownSyntax = {
  heading: /^#{1,6}\s+.+$/,
  bold: /\*\*.+?\*\*|__.+?__/,
  italic: /\*.+?\*|_.+?_/,
  code: /`.+?`/,
  codeBlock: /```[\s\S]*?```/,
  link: /\[.+?\]\(.+?\)/,
  image: /!\[.*?\]\(.+?\)/,
  list: /^[\*\-\+]\s+.+$|^\d+\.\s+.+$/,
  blockquote: /^>\s+.+$/,
  hr: /^---+$|^\*\*\*+$|^___+$/,
  taskList: /^[\*\-\+]\s+\[[x\s]\]\s+.+$/i
}

export function detectSyntax(line: string): string[] {
  const detected: string[] = []
  
  for (const [name, pattern] of Object.entries(MarkdownSyntax)) {
    if (pattern.test(line)) {
      detected.push(name)
    }
  }
  
  return detected
}
