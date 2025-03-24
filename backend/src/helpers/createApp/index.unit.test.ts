import { createApp } from '.';
import express from 'express';

interface ExpressLayer {
    handle?: express.RequestHandler;
    route?: {
        path: string;
    };
    name?: string;
    regexp?: RegExp;
}

describe('createApp (Unit)', () => {
    it('should return an express instance', () => {
        const app = createApp();
        expect(app).toBeInstanceOf(Function);
        expect(app).toHaveProperty('use');
        expect(app).toHaveProperty('listen');
        const routerStack = (app as any)._router.stack as ExpressLayer[];
        const middlewares = routerStack
            .filter(layer => !!layer.handle)
            .map(layer => layer.handle?.name);
        expect(middlewares).toContain('jsonParser');
        expect(middlewares).toContain('corsMiddleware');
    });

    it('should have the correct route handlers mounted', () => {
        const app = createApp();
        const routerStack = (app as any)._router.stack as any[];
        const routes = routerStack
            .filter(layer => layer.name === 'router')
            .flatMap(layer => {
                const path = layer.regexp.source
                    .replace('^', '')
                    .replace('\\/?(?=\\/|$)', '')
                    .replace(/\\/g, '/');
                return {
                    path,
                    methods: layer.handle.stack.map((route: any) =>
                        Object.keys(route.route?.methods || {})
                    )
                };
            });
        expect(routes.map(r => r.path)).toEqual(
            expect.arrayContaining([
                '//api//form',
                '//api//students',
                '//api//leaderboard'
            ])
        );
    });
});
