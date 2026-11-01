export interface Env {
  AI: Ai;
}

type ChatMessage = {
  role: "user" | "assistant";
  content: string;
};

const SYSTEM_PROMPT = `Сенинг исминг Тимур. Сен фойдаланувчига ўзбекча (кирилл) ёки русча мулойим, самимий ва тушунарли жавоб берадиган шахсий AI ёрдамчисан. Фойдаланувчи қайси тилда ёзса, шу тилда жавоб бер. Саволга тўғридан-тўғри жавоб бер, билмаган нарсангни тўқима. Қисқа ва фойдали бўл. Сен Android иловасида ишлаяпсан; телефондаги буйруқларни фақат илова бажара олади, ўзинг бажардим деб айтма.`;

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, X-Timur-App",
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store"
    };

    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    if (new URL(request.url).pathname !== "/chat") {
      return Response.json({ error: "Not found" }, { status: 404, headers: cors });
    }
    if (request.method !== "POST") {
      return Response.json({ error: "Method not allowed" }, { status: 405, headers: cors });
    }
    if (request.headers.get("X-Timur-App") !== "timur-android-v1") {
      return Response.json({ error: "Unauthorized app request" }, { status: 401, headers: cors });
    }

    try {
      const body = await request.json() as { messages?: ChatMessage[]; language?: string };
      const messages = Array.isArray(body.messages) ? body.messages.slice(-12) : [];
      const valid = messages.filter(m =>
        (m.role === "user" || m.role === "assistant") &&
        typeof m.content === "string" && m.content.trim().length > 0
      );
      if (!valid.length || valid[valid.length - 1].role !== "user") {
        return Response.json({ error: "A user message is required" }, { status: 400, headers: cors });
      }

      const result = await env.AI.run("@cf/meta/llama-3.2-3b-instruct", {
        messages: [
          { role: "system", content: SYSTEM_PROMPT },
          ...valid.map(m => ({ role: m.role, content: m.content.slice(0, 3000) }))
        ],
        max_tokens: 450,
        temperature: 0.65
      }) as { response?: string };

      const answer = result?.response?.trim();
      if (!answer) throw new Error("Empty AI response");
      return Response.json({ reply: answer }, { headers: cors });
    } catch (error) {
      const message = error instanceof Error ? error.message : "AI request failed";
      return Response.json({ error: message }, { status: 502, headers: cors });
    }
  }
};
