import { AuthenticationService } from 'src/app/_service/authentication.service'

import { Component, OnInit, ElementRef } from '@angular/core'
import { Router } from '@angular/router'
import {Title} from "@angular/platform-browser";
import { NgbCarouselConfig } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'public-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class PublicHome implements OnInit {

  images = [
    {
      title: 'Operações por PDF',
      short: "Envie suas notas de corretagens mensais e deixe que o serviço faça o resto."+
    "Você pode enviar uma ou várias notas de corretagem.",
      src: "/assets/screenshots/send-brokerage-notes.png"
    },
    {
      title: 'Veja o status para cada nota de corretagem',
      short: "Nessa seção, você pode verificar se algo deu errado com as suas notas de corretagem."+
      " Dependendo da quantidade de requisições por dia, a sua nota pode ficar na fila para ser processada mais tarde.",
      src: "/assets/screenshots/brokerage-notes-list.png"
    },
    {
      title: 'Edite suas operações',
      short: "Se você é mais detalhista, verifique todas as suas operações. Você pode"+
      "inserir, remover ou editar.",
      src: "/assets/screenshots/operations-list.png"
    },
    {
      title: "Veja a sua performance nos gráficos",
      short: "Obtenha seu resultado mensal através dos gráficos. Você tem gráficos de"+
      "lucro/prejuízo mensal. As possibilidades são gráficos no contexto geral, por "+
      "tipo de mercado e por tipo de operação. A"+
      "ideia é que você possa pagar sua darf"+
      "ou fazer a declaração anual apenas olhando esses gráficos.",
      src: "/assets/screenshots/dashboard.png"
    }
  ];

  constructor(
    public elementRef: ElementRef,
    private authenticationService: AuthenticationService,
    private router: Router,
    private titleService:Title,
    private crouselConfig: NgbCarouselConfig
  ) {
    this.titleService.setTitle("Relatório Leão - Página inicial.");
    crouselConfig.interval = 2000;
    crouselConfig.keyboard = true;
    crouselConfig.pauseOnHover = true;
  }

  ngOnInit() {

    if (this.authenticationService.currentUserValue != null) {
      this.router.navigate(['/home'])
    }
  }

  
}
