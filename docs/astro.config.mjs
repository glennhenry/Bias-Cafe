// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";
import starlightThemeObsidian from "starlight-theme-obsidian";
import rehypeExternalLinks from "rehype-external-links";
import externalLinkIcon from "./src/assets/externalLinkIcon.js";
import { fontHeadTags } from "./src/assets/headlinks.js";

// https://astro.build/config
export default defineConfig({
  // site: "", // deployment URL
  base: "docs",
  markdown: {
    rehypePlugins: [[rehypeExternalLinks, externalLinkIcon]],
  },
  integrations: [
    starlight({
      head: [...fontHeadTags],
      plugins: [starlightThemeObsidian()],
      favicon: "favicon.ico",
      customCss: ["./src/assets/custom.css"],
      tableOfContents: { minHeadingLevel: 2, maxHeadingLevel: 6 },
      credits: true,
      lastUpdated: true,
      title: "Bias Cafe",
      components: {
        Pagination: "./src/components/Pagination.astro",
        PageFrame: "./src/components/PageFrame.astro",
      },
      editLink: {
        baseUrl: "https://github.com/glennhenry/Bias-Cafe/edit/main/",
      },
      social: [
        {
          icon: "github",
          label: "GitHub",
          href: "https://github.com/glennhenry/Bias-Cafe",
        },
      ],
      sidebar: [
        { label: "Intro", slug: "index" }
      ],
    }),
  ],
});
