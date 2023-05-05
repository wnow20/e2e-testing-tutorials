import postLoadConfig from "postcss-load-config";
import fs from "fs";
import postcss from "postcss";

export function postCssPlugin(extension = ['.css']) {
  return {
    name: 'esbuild-postcss',
    async setup(build) {
      const postcssConfig = await postLoadConfig();

      const dotExts = extension
        .map(x => x.replaceAll(".", "\\."))
        .join("|");
      const targetFileRegExp = new RegExp(`(${dotExts})$`);
      build.onLoad({filter: targetFileRegExp}, async (args) => {
        const css = fs.readFileSync(args.path, 'utf8');
        const result = await postcss(postcssConfig.plugins).process(css, {
          ...postcssConfig.options,
          from: args.path,
        });
        return {contents: result.css, loader: 'css'};
      });
    },
  };
}
