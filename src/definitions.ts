export interface MediaPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
