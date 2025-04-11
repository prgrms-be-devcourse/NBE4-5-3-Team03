import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  /* CAUTION!!! ALL TYPECHECKS WILL BE IGNORED WHEN BUILDING!!! */
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://backend:8080/api/:path*', // spring 서버로 포워딩
      },
    ];
  },
};

export default nextConfig;
