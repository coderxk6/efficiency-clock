import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    nickname: localStorage.getItem('nickname') || '',
    isGuest: localStorage.getItem('isGuest') === 'true',
    rank: '凡人',
    exp: 0
  }),
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.username
  },
  actions: {
    setAuthData(authData) {
      this.token = authData.token;
      this.username = authData.username;
      this.nickname = authData.nickname;
      this.isGuest = authData.isGuest;
      
      localStorage.setItem('token', this.token);
      localStorage.setItem('username', this.username);
      localStorage.setItem('nickname', this.nickname);
      localStorage.setItem('isGuest', this.isGuest);
    },
    logout() {
      if (this.isGuest) {
        // preserve guest token in dedicated keys if needed, like previously done
        const guestToken = localStorage.getItem('token');
        if(guestToken) {
           localStorage.setItem('guestToken', guestToken);
           localStorage.setItem('guestUsername', localStorage.getItem('username'));
           localStorage.setItem('guestNickname', localStorage.getItem('nickname'));
        }
      }
      this.token = '';
      this.username = '';
      this.nickname = '';
      this.isGuest = false;
      this.rank = '凡人';
      this.exp = 0;
      
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('nickname');
      localStorage.removeItem('isGuest');
    },
    updateCultivation(rank, exp) {
      this.rank = rank;
      this.exp = exp;
    }
  }
});
