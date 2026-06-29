import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';

vi.mock('axios');

describe('httpClient', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.resetModules();
    localStorage.clear();
  });

  it('should create axios instance with correct baseURL and timeout', async () => {
    const mockInstance = {
      defaults: {},
      interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
      get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), request: vi.fn(),
    };
    vi.mocked(axios.create).mockReturnValue(mockInstance);

    const { default: httpClient } = await import('./httpClient');

    expect(axios.create).toHaveBeenCalledWith({
      baseURL: 'http://localhost:8097',
      timeout: 15000,
    });
    expect(httpClient).toBeDefined();
  });

  it('should register interceptors', async () => {
    const useReq = vi.fn();
    const useRes = vi.fn();
    const mockInstance = {
      defaults: {},
      interceptors: { request: { use: useReq }, response: { use: useRes } },
      get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), request: vi.fn(),
    };
    vi.mocked(axios.create).mockReturnValue(mockInstance);

    await import('./httpClient');

    expect(useReq).toHaveBeenCalledOnce();
    expect(useRes).toHaveBeenCalledOnce();
  });

  it('should have standard HTTP methods', async () => {
    const mockInstance = {
      defaults: {},
      interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
      get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), request: vi.fn(),
    };
    vi.mocked(axios.create).mockReturnValue(mockInstance);

    const { default: httpClient } = await import('./httpClient');

    expect(typeof httpClient.get).toBe('function');
    expect(typeof httpClient.post).toBe('function');
    expect(typeof httpClient.put).toBe('function');
    expect(typeof httpClient.delete).toBe('function');
  });
});
