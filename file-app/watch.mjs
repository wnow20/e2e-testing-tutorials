import * as esbuild from 'esbuild';
import {postCssPlugin} from "./postCssPlugin.mjs";
import {htmlPlugin} from "@craftamap/esbuild-plugin-html";
import fs from "fs";
import path from "path";

const destDir = './dist';
const publicDir = './public';
if (fs.existsSync(destDir)) {
  await fs.promises.rm(destDir, {
    recursive: true,
  });
}
await fs.promises.mkdir(destDir);
let publicFile = fs.readdirSync(publicDir);
publicFile.forEach(async (subFile) => {
  await fs.promises.copyFile(path.join(publicDir, subFile), path.join(destDir, subFile));
});

let serverBuildContext = await esbuild.context({
  entryPoints: ['server.ts'],
  outfile: 'server.js',
  platform: 'node',
  target: ['node16'],
  minify: false,
});
serverBuildContext.watch()

let staticContext = await esbuild.context({
  entryPoints: ['src/app.tsx'],
  bundle: true,
  metafile: true,
  outdir: 'dist',
  minify: false,
  sourcemap: true,
  target: ['chrome109', 'edge110', 'firefox110', 'safari15'],
  plugins: [
    postCssPlugin(),
    htmlPlugin({
      files: [
        {
          entryPoints: [
            'src/app.tsx',
          ],
          filename: 'index.html',
          htmlTemplate: 'public/index.html',
        },
      ]
    })
  ],
});
staticContext.watch()
